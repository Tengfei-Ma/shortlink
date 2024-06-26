package org.mtf.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mtf.shortlink.project.common.convention.exception.ClientException;
import org.mtf.shortlink.project.common.convention.exception.ServiceException;
import org.mtf.shortlink.project.common.enums.ShortlinkErrorCodeEnum;
import org.mtf.shortlink.project.config.GotoDomainWhiteListConfiguration;
import org.mtf.shortlink.project.dao.entity.*;
import org.mtf.shortlink.project.dao.mapper.*;
import org.mtf.shortlink.project.dto.biz.ShortlinkStatsRecordDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkBatchCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.project.dto.resp.*;
import org.mtf.shortlink.project.mq.producer.ShortlinkStatsSaveProducer;
import org.mtf.shortlink.project.service.LinkStatsTodayService;
import org.mtf.shortlink.project.service.ShortlinkService;
import org.mtf.shortlink.project.toolkit.HashUtil;
import org.mtf.shortlink.project.toolkit.LinkUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.*;
import static org.mtf.shortlink.project.common.enums.ValidDateTypeEnum.PERMANENT;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortlinkServiceImpl extends ServiceImpl<ShortlinkMapper, ShortlinkDO> implements ShortlinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortlinkGotoMapper shortlinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final LinkStatsTodayMapper linkStatsTodayMapper;

    private final LinkStatsTodayService linkStatsTodayService;

    private final ShortlinkStatsSaveProducer shortlinkStatsSaveProducer;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;


    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Value("${short-link.domain.default}")
    private String defaultDomain;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortlinkCreateRespDTO createShortLink(ShortlinkCreateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());

        ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);
        String shortUri = generateShortUri(requestParam.getOriginUrl());
        String fullShortUrl = defaultDomain + "/" + shortUri;
        shortlinkDO.setDomain(defaultDomain);
        shortlinkDO.setShortUri(shortUri);
        shortlinkDO.setFullShortUrl(fullShortUrl);
        shortlinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
        shortlinkDO.setClickNum(0);
        shortlinkDO.setEnableStatus(0);
        shortlinkDO.setTotalPv(0);
        shortlinkDO.setTotalUv(0);
        shortlinkDO.setTotalUip(0);
        shortlinkDO.setDelTime(0L);

        ShortlinkGotoDO shortlinkGotoDO = new ShortlinkGotoDO();
        shortlinkGotoDO.setFullShortUrl(fullShortUrl);
        shortlinkGotoDO.setGid(requestParam.getGid());
        try {
            baseMapper.insert(shortlinkDO);
            shortlinkGotoMapper.insert(shortlinkGotoDO);
        } catch (DuplicateKeyException exception) {
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(ShortlinkErrorCodeEnum.SHORTLINK_EXIST);
        }
        //缓存预热，将刚创建的短链接存入缓存并根据有效期设置缓存过期时间
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getShortlinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        ShortlinkCreateRespDTO shortlinkCreateRespDTO = new ShortlinkCreateRespDTO();
        shortlinkCreateRespDTO.setGid(requestParam.getGid());
        shortlinkCreateRespDTO.setOriginUrl(requestParam.getOriginUrl());
        shortlinkCreateRespDTO.setFullShortUrl(fullShortUrl);
        return shortlinkCreateRespDTO;
    }

    @Override
    public ShortlinkCreateRespDTO createShortlinkByLock(ShortlinkCreateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        String fullShortUrl;
        RLock lock = redissonClient.getLock(SHORT_LINK_CREATE_LOCK_KEY);
        lock.lock();
        try {
            ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);
            String shortUri = generateShortUri(requestParam.getOriginUrl());
            fullShortUrl = defaultDomain + "/" + shortUri;
            shortlinkDO.setDomain(defaultDomain);
            shortlinkDO.setShortUri(shortUri);
            shortlinkDO.setFullShortUrl(fullShortUrl);
            shortlinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
            shortlinkDO.setClickNum(0);
            shortlinkDO.setEnableStatus(0);
            shortlinkDO.setTotalPv(0);
            shortlinkDO.setTotalUv(0);
            shortlinkDO.setTotalUip(0);
            shortlinkDO.setDelTime(0L);

            ShortlinkGotoDO shortlinkGotoDO = new ShortlinkGotoDO();
            shortlinkGotoDO.setFullShortUrl(fullShortUrl);
            shortlinkGotoDO.setGid(requestParam.getGid());
            try {
                baseMapper.insert(shortlinkDO);
                shortlinkGotoMapper.insert(shortlinkGotoDO);
            } catch (DuplicateKeyException ex) {
                throw new ServiceException(ShortlinkErrorCodeEnum.SHORTLINK_EXIST);
            }
            //缓存预热，将刚创建的短链接存入缓存并根据有效期设置缓存过期时间
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    requestParam.getOriginUrl(),
                    LinkUtil.getShortlinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS
            );
        } finally {
            lock.unlock();
        }
        ShortlinkCreateRespDTO shortlinkCreateRespDTO = new ShortlinkCreateRespDTO();
        shortlinkCreateRespDTO.setGid(requestParam.getGid());
        shortlinkCreateRespDTO.setOriginUrl(requestParam.getOriginUrl());
        shortlinkCreateRespDTO.setFullShortUrl(fullShortUrl);
        return shortlinkCreateRespDTO;
    }

    @Override
    public IPage<ShortlinkPageRespDTO> pageShortlink(ShortlinkPageReqDTO requestParam) {
        IPage<ShortlinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(shortlinkDO -> BeanUtil.toBean(shortlinkDO, ShortlinkPageRespDTO.class));
    }

    @Override
    public List<ShortlinkGroupCountRespDTO> listGroupShortlinkCount(List<String> requestParam) {
        QueryWrapper<ShortlinkDO> queryWrapper = Wrappers.query(new ShortlinkDO())
                .select("gid, count(*) as shortlinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(maps, ShortlinkGroupCountRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortlink(ShortlinkUpdateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());

        LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortlinkDO::getDelFlag, 0)
                .eq(ShortlinkDO::getEnableStatus, 0);
        ShortlinkDO hasShortlinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortlinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(requestParam.getOriginGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortlinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                    .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortlinkDO::getGid, requestParam.getGid())
                    .eq(ShortlinkDO::getDelFlag, 0)
                    .eq(ShortlinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), PERMANENT.getType()), ShortlinkDO::getValidDate, null);
            ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);

            shortlinkDO.setShortUri(hasShortlinkDO.getShortUri());
            baseMapper.update(shortlinkDO, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            rLock.lock();
            try {
                LambdaUpdateWrapper<ShortlinkDO> deleteWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                        .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortlinkDO::getGid, hasShortlinkDO.getGid())
                        .eq(ShortlinkDO::getDelFlag, 0)
                        .eq(ShortlinkDO::getEnableStatus, 0);
                ShortlinkDO deleteShortlinkDO = new ShortlinkDO();
                deleteShortlinkDO.setDelTime(System.currentTimeMillis());
                deleteShortlinkDO.setDelFlag(1);
                baseMapper.update(deleteShortlinkDO, deleteWrapper);
                ShortlinkDO insertShortLinkDO = new ShortlinkDO();
                insertShortLinkDO.setDomain(hasShortlinkDO.getDomain());
                insertShortLinkDO.setShortUri(hasShortlinkDO.getShortUri());
                insertShortLinkDO.setFullShortUrl(requestParam.getFullShortUrl());
                insertShortLinkDO.setOriginUrl(requestParam.getOriginUrl());
                insertShortLinkDO.setClickNum(0);
                insertShortLinkDO.setGid(requestParam.getGid());
                insertShortLinkDO.setFavicon(hasShortlinkDO.getFavicon());
                insertShortLinkDO.setEnableStatus(hasShortlinkDO.getEnableStatus());
                insertShortLinkDO.setCreateType(hasShortlinkDO.getCreateType());
                insertShortLinkDO.setValidDateType(requestParam.getValidDateType());
                if (Objects.equals(requestParam.getValidDateType(), PERMANENT.getType())) {
                    insertShortLinkDO.setValidDate(null);
                } else {
                    insertShortLinkDO.setValidDate(requestParam.getValidDate());
                }
                insertShortLinkDO.setDescribe(requestParam.getDescribe());
                baseMapper.insert(insertShortLinkDO);

                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<ShortlinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortlinkGotoDO.class)
                        .eq(ShortlinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortlinkGotoDO::getGid, hasShortlinkDO.getGid());
                ShortlinkGotoDO shortLinkGotoDO = shortlinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortlinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortlinkGotoMapper.insert(shortLinkGotoDO);
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = new LinkAccessStatsDO();
                linkAccessStatsDO.setGid(requestParam.getGid());
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocalStatsDO> linkLocalStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocalStatsDO.class)
                        .eq(LinkLocalStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocalStatsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkLocalStatsDO::getDelFlag, 0);
                LinkLocalStatsDO linkLocalStatsDO = new LinkLocalStatsDO();
                linkLocalStatsDO.setGid(requestParam.getGid());
                linkLocalStatsMapper.update(linkLocalStatsDO, linkLocalStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = new LinkOsStatsDO();
                linkOsStatsDO.setGid(requestParam.getGid());
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = new LinkBrowserStatsDO();
                linkBrowserStatsDO.setGid(requestParam.getGid());
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = new LinkDeviceStatsDO();
                linkDeviceStatsDO.setGid(requestParam.getGid());
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = new LinkNetworkStatsDO();
                linkNetworkStatsDO.setGid(requestParam.getGid());
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, hasShortlinkDO.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = new LinkAccessLogsDO();
                linkAccessLogsDO.setGid(requestParam.getGid());
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } finally {
                rLock.unlock();
            }
        }

        if (!Objects.equals(hasShortlinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortlinkDO.getValidDate(), requestParam.getValidDate())
                || !Objects.equals(hasShortlinkDO.getOriginUrl(), requestParam.getOriginUrl())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            if (hasShortlinkDO.getValidDate() != null && hasShortlinkDO.getValidDate().before(new Date())) {
                if (Objects.equals(requestParam.getValidDateType(), PERMANENT.getType()) || requestParam.getValidDate().after(new Date())) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            ShortlinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortlinkStatistic(statsRecord);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        String gotoIsNull = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNull)) {
            //gotoIsNull="-"，布隆过滤器误判了，布隆过滤器认为存在但mysql不存在，移至回收站是设置为不可用，刚好也符合误判情况
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ShortlinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortlinkStatistic(statsRecord);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            gotoIsNull = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(gotoIsNull)) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortlinkGotoDO> gidQueryWrapper = Wrappers.lambdaQuery(ShortlinkGotoDO.class)
                    .eq(ShortlinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortlinkGotoDO shortlinkGotoDO = shortlinkGotoMapper.selectOne(gidQueryWrapper);
            if (shortlinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30L, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                    .eq(ShortlinkDO::getGid, shortlinkGotoDO.getGid())
                    .eq(ShortlinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortlinkDO::getEnableStatus, 0)
                    .eq(ShortlinkDO::getDelFlag, 0);
            ShortlinkDO shortlinkDO = baseMapper.selectOne(queryWrapper);
            if (shortlinkDO == null || (shortlinkDO.getValidDate() != null && shortlinkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30L, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortlinkDO.getOriginUrl(),
                    LinkUtil.getShortlinkCacheValidTime(shortlinkDO.getValidDate()), TimeUnit.MICROSECONDS
            );
            ShortlinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortlinkStatistic(statsRecord);
            ((HttpServletResponse) response).sendRedirect(shortlinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ShortlinkBatchCreateRespDTO batchCreateShortlink(ShortlinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortlinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortlinkCreateReqDTO shortlinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortlinkCreateReqDTO.class);
            shortlinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortlinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortlinkCreateRespDTO shortlink = createShortLink(shortlinkCreateReqDTO);
                ShortlinkBaseInfoRespDTO linkBaseInfoRespDTO = new ShortlinkBaseInfoRespDTO();
                linkBaseInfoRespDTO.setFullShortUrl(shortlink.getFullShortUrl());
                linkBaseInfoRespDTO.setOriginUrl(shortlink.getOriginUrl());
                linkBaseInfoRespDTO.setDescribe(describes.get(i));
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        ShortlinkBatchCreateRespDTO shortlinkBatchCreateRespDTO = new ShortlinkBatchCreateRespDTO();
        shortlinkBatchCreateRespDTO.setTotal(result.size());
        shortlinkBatchCreateRespDTO.setBaseLinkInfos(result);
        return shortlinkBatchCreateRespDTO;
    }

    private void shortlinkStatistic(ShortlinkStatsRecordDTO statsRecord) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        // 消息队列为什么选用RocketMQ？详情查看：https://nageoffer.com/shortlink/question
        shortlinkStatsSaveProducer.send(producerMap);
    }

    /**
     * 构建短链接监控数据传输对象
     */
    private ShortlinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = request.getRemoteAddr();
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        ShortlinkStatsRecordDTO shortlinkStatsRecordDTO = new ShortlinkStatsRecordDTO();
        shortlinkStatsRecordDTO.setFullShortUrl(fullShortUrl);
        shortlinkStatsRecordDTO.setRemoteAddr(remoteAddr);
        shortlinkStatsRecordDTO.setOs(os);
        shortlinkStatsRecordDTO.setBrowser(browser);
        shortlinkStatsRecordDTO.setDevice(device);
        shortlinkStatsRecordDTO.setNetwork(network);
        shortlinkStatsRecordDTO.setUv(uv.get());
        shortlinkStatsRecordDTO.setUvFirstFlag(uvFirstFlag.get());
        shortlinkStatsRecordDTO.setUipFirstFlag(uipFirstFlag);
        return shortlinkStatsRecordDTO;
    }

    /**
     * 根据原始链接生成短链接，防止重复，尝试10次,用布隆过滤器实现
     */
    private String generateShortUri(String originUrl) {
        int customGenerateCount = 1;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException(ShortlinkErrorCodeEnum.TRY_GENERATE_ERROR);
            }
            shortUri = HashUtil.hashToBase62(originUrl + UUID.randomUUID());
            if (!shortUriCreateCachePenetrationBloomFilter.contains(defaultDomain + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
    /**
     * 根据原始链接生成短链接，防止重复，尝试10次，用分布式锁实现
     */
    private String generateShortUriByLock(String gid,String originUrl) {
        int customGenerateCount = 1;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException(ShortlinkErrorCodeEnum.TRY_GENERATE_ERROR);
            }
            shortUri = HashUtil.hashToBase62(originUrl + UUID.randomUUID());
            LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                    .eq(ShortlinkDO::getGid, gid)
                    .eq(ShortlinkDO::getFullShortUrl, defaultDomain + "/" + shortUri)
                    .eq(ShortlinkDO::getDelFlag, 0);
            ShortlinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    /**
     * 根据原始链接获取网站图标
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    /**
     * 验证白名单
     */
    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }
}
