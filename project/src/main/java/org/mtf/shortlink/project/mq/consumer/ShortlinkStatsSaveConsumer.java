/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mtf.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtf.shortlink.project.common.convention.exception.ServiceException;
import org.mtf.shortlink.project.dao.entity.*;
import org.mtf.shortlink.project.dao.mapper.*;
import org.mtf.shortlink.project.dto.biz.ShortlinkStatsRecordDTO;
import org.mtf.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.LOCK_GID_UPDATE_KEY;
import static org.mtf.shortlink.project.common.constant.ShortlinkConstant.AMAP_REMOTE_URL;


/**
 * 短链接监控状态保存消息队列消费者
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortlinkStatsSaveConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final ShortlinkMapper shortlinkMapper;
    private final ShortlinkGotoMapper shortlinkGotoMapper;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocalAmapKey;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
        if (!messageQueueIdempotentHandler.isMessageProcessed(id.toString())) {
            // 判断当前的这个消息流程是否执行完成
            if (messageQueueIdempotentHandler.isAccomplish(id.toString())) {
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        try {
            Map<String, String> producerMap = message.getValue();
            ShortlinkStatsRecordDTO statsRecord = JSON.parseObject(producerMap.get("statsRecord"), ShortlinkStatsRecordDTO.class);
            actualSaveShortlinkStats(statsRecord);
            stringRedisTemplate.opsForStream().delete(Objects.requireNonNull(stream), id.getValue());
        } catch (Throwable ex) {
            // 消费失败要重试，删除redis中的幂等标识
            messageQueueIdempotentHandler.delMessageProcessed(id.toString());
            log.error("记录短链接监控消费异常", ex);
            throw ex;
        }
        messageQueueIdempotentHandler.setAccomplish(id.toString());
    }

    public void actualSaveShortlinkStats(ShortlinkStatsRecordDTO statsRecord) {
        String fullShortUrl = statsRecord.getFullShortUrl();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            LambdaQueryWrapper<ShortlinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkGotoDO.class)
                    .eq(ShortlinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortlinkGotoDO shortlinkGotoDO = shortlinkGotoMapper.selectOne(queryWrapper);
            String gid = shortlinkGotoDO.getGid();
            int hour = DateUtil.hour(new Date(), true);
            int week = DateUtil.dayOfWeekEnum(new Date()).getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = new LinkAccessStatsDO();
            linkAccessStatsDO.setPv(1);
            linkAccessStatsDO.setUv(statsRecord.getUvFirstFlag() ? 1 : 0);
            linkAccessStatsDO.setUip(statsRecord.getUipFirstFlag() ? 1 : 0);
            linkAccessStatsDO.setFullShortUrl(fullShortUrl);
            linkAccessStatsDO.setGid(gid);
            linkAccessStatsDO.setHour(hour);
            linkAccessStatsDO.setWeekday(week);
            linkAccessStatsDO.setDate(new Date());
            linkAccessStatsMapper.shortlinkStats(linkAccessStatsDO);

            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocalAmapKey);
            localeParamMap.put("ip", statsRecord.getRemoteAddr());
            String resp = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localObj = JSON.parseObject(resp);
            String infoCode = localObj.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localObj.getString("province");
                String city = localObj.getString("city");
                String adCode = localObj.getString("adcode");
                LinkLocalStatsDO linkLocalStatsDO = new LinkLocalStatsDO();
                linkLocalStatsDO.setFullShortUrl(fullShortUrl);
                linkLocalStatsDO.setGid(gid);
                linkLocalStatsDO.setDate(new Date());
                linkLocalStatsDO.setCnt(1);
                linkLocalStatsDO.setCountry("中国");
                linkLocalStatsDO.setProvince(actualProvince = StrUtil.equals(province, "[]") ? "unknown" : province);
                linkLocalStatsDO.setCity(actualCity = StrUtil.equals(city, "[]") ? "unknown" : city);
                linkLocalStatsDO.setAdcode(StrUtil.equals(adCode, "[]") ? "unknown" : adCode);
                linkLocalStatsMapper.shortlinkLocalStats(linkLocalStatsDO);
            }
            LinkOsStatsDO linkOsStatsDO = new LinkOsStatsDO();
            linkOsStatsDO.setFullShortUrl(fullShortUrl);
            linkOsStatsDO.setGid(gid);
            linkOsStatsDO.setDate(new Date());
            linkOsStatsDO.setCnt(1);
            linkOsStatsDO.setOs(statsRecord.getOs());
            linkOsStatsMapper.shortlinkOsStats(linkOsStatsDO);

            LinkBrowserStatsDO linkBrowserStatsDO = new LinkBrowserStatsDO();
            linkBrowserStatsDO.setFullShortUrl(fullShortUrl);
            linkBrowserStatsDO.setGid(gid);
            linkBrowserStatsDO.setDate(new Date());
            linkBrowserStatsDO.setCnt(1);
            linkBrowserStatsDO.setBrowser(statsRecord.getBrowser());
            linkBrowserStatsMapper.shortlinkBrowserStats(linkBrowserStatsDO);

            LinkDeviceStatsDO linkDeviceStatsDO = new LinkDeviceStatsDO();
            linkDeviceStatsDO.setFullShortUrl(fullShortUrl);
            linkDeviceStatsDO.setGid(gid);
            linkDeviceStatsDO.setDate(new Date());
            linkDeviceStatsDO.setCnt(1);
            linkDeviceStatsDO.setDevice(statsRecord.getDevice());
            linkDeviceStatsMapper.shortlinkDeviceStats(linkDeviceStatsDO);

            LinkNetworkStatsDO linkNetworkStatsDO = new LinkNetworkStatsDO();
            linkNetworkStatsDO.setFullShortUrl(fullShortUrl);
            linkNetworkStatsDO.setGid(gid);
            linkNetworkStatsDO.setDate(new Date());
            linkNetworkStatsDO.setCnt(1);
            linkNetworkStatsDO.setNetwork(statsRecord.getNetwork());
            linkNetworkStatsMapper.shortlinkNetworkStats(linkNetworkStatsDO);

            LinkAccessLogsDO linkAccessLogsDO = new LinkAccessLogsDO();
            linkAccessLogsDO.setFullShortUrl(fullShortUrl);
            linkAccessLogsDO.setGid(gid);
            linkAccessLogsDO.setUser(statsRecord.getUv());
            linkAccessLogsDO.setBrowser(statsRecord.getBrowser());
            linkAccessLogsDO.setOs(statsRecord.getOs());
            linkAccessLogsDO.setIp(statsRecord.getRemoteAddr());
            linkAccessLogsDO.setNetwork(statsRecord.getNetwork());
            linkAccessLogsDO.setDevice(statsRecord.getDevice());
            linkAccessLogsDO.setLocal(StrUtil.join("-", "中国", actualProvince, actualCity));
            linkAccessLogsMapper.insert(linkAccessLogsDO);

            shortlinkMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
            LinkStatsTodayDO linkStatsTodayDO = new LinkStatsTodayDO();
            linkStatsTodayDO.setFullShortUrl(fullShortUrl);
            linkStatsTodayDO.setGid(gid);
            linkStatsTodayDO.setDate(new Date());
            linkStatsTodayDO.setTodayPv(1);
            linkStatsTodayDO.setTodayUv(statsRecord.getUvFirstFlag() ? 1 : 0);
            linkStatsTodayDO.setTodayUip(statsRecord.getUipFirstFlag() ? 1 : 0);
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        } finally {
            rLock.unlock();
        }
    }
}
