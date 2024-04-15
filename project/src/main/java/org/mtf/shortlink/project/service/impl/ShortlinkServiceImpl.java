package org.mtf.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.mtf.shortlink.project.common.enums.ValidDateTypeEnum;
import org.mtf.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.mtf.shortlink.project.dao.entity.LinkLocalStatsDO;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dao.entity.ShortlinkGotoDO;
import org.mtf.shortlink.project.dao.mapper.LinkAccessStatusMapper;
import org.mtf.shortlink.project.dao.mapper.LinkLocalStatsMapper;
import org.mtf.shortlink.project.dao.mapper.ShortlinkGotoMapper;
import org.mtf.shortlink.project.dao.mapper.ShortlinkMapper;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.service.ShortlinkService;
import org.mtf.shortlink.project.toolkit.HashUtil;
import org.mtf.shortlink.project.toolkit.LinkUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.*;
import static org.mtf.shortlink.project.common.constant.ShortlinkConstant.AMAP_REMOTE_URL;

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

    private final LinkAccessStatusMapper linkAccessStatusMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;



    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Override
    public ShortlinkCreateRespDTO createShortLink(ShortlinkCreateReqDTO requestParam) {
        ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);
        String shortUri = generateShortUri(requestParam.getDomain(), requestParam.getOriginUrl());
        String fullShortUrl = requestParam.getDomain() + "/" + shortUri;

        shortlinkDO.setShortUri(shortUri);
        shortlinkDO.setFullShortUrl(fullShortUrl);
        shortlinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
        shortlinkDO.setClickNum(0);
        shortlinkDO.setEnableStatus(0);

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
                LinkUtil.getShortlinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        ShortlinkCreateRespDTO shortlinkCreateRespDTO = new ShortlinkCreateRespDTO();
        shortlinkCreateRespDTO.setGid(requestParam.getGid());
        shortlinkCreateRespDTO.setOriginUrl(requestParam.getOriginUrl());
        shortlinkCreateRespDTO.setFullShortUrl(fullShortUrl);
        return shortlinkCreateRespDTO;
    }

    @Override
    public IPage<ShortlinkPageRespDTO> pageShortlink(ShortlinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getGid())
                .eq(ShortlinkDO::getEnableStatus, 0)
                .eq(ShortlinkDO::getDelFlag, 0)
                .orderByDesc(ShortlinkDO::getCreateTime);
        Page<ShortlinkDO> page = page(requestParam, queryWrapper);
        return page.convert(shortlinkDO -> BeanUtil.toBean(shortlinkDO, ShortlinkPageRespDTO.class));
    }

    @Override
    public List<ShortlinkGroupCountRespDTO> listGroupShortlinkCount(List<String> requestParam) {
        QueryWrapper<ShortlinkDO> queryWrapper = Wrappers.query(new ShortlinkDO())
                .select("gid, count(*) as shortlinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(maps, ShortlinkGroupCountRespDTO.class);
    }

    @Override
    public void updateShortlink(ShortlinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortlinkDO::getDelFlag, 0)
                .eq(ShortlinkDO::getEnableStatus, 0);
        ShortlinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(requestParam.getOriginGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortlinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                    .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortlinkDO::getGid, requestParam.getGid())
                    .eq(ShortlinkDO::getDelFlag, 0)
                    .eq(ShortlinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortlinkDO::getValidDate, null);
            ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);

            shortlinkDO.setShortUri(hasShortLinkDO.getShortUri());
            baseMapper.update(shortlinkDO, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortlinkDO> deleteWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                    .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortlinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortlinkDO::getDelFlag, 0)
                    .eq(ShortlinkDO::getEnableStatus, 0);
            ShortlinkDO deleteShortlinkDO = new ShortlinkDO();
            deleteShortlinkDO.setDelFlag(1);
            baseMapper.update(deleteShortlinkDO, deleteWrapper);
            ShortlinkDO insertShortLinkDO = new ShortlinkDO();
            insertShortLinkDO.setDomain(hasShortLinkDO.getDomain());
            insertShortLinkDO.setShortUri(hasShortLinkDO.getShortUri());
            insertShortLinkDO.setFullShortUrl(requestParam.getFullShortUrl());
            insertShortLinkDO.setOriginUrl(requestParam.getOriginUrl());
            insertShortLinkDO.setClickNum(0);
            insertShortLinkDO.setGid(requestParam.getGid());
            insertShortLinkDO.setFavicon(hasShortLinkDO.getFavicon());
            insertShortLinkDO.setEnableStatus(hasShortLinkDO.getEnableStatus());
            insertShortLinkDO.setCreateType(hasShortLinkDO.getCreateType());
            insertShortLinkDO.setValidDateType(requestParam.getValidDateType());
            if (Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType())) {
                insertShortLinkDO.setValidDate(null);
            } else {
                insertShortLinkDO.setValidDate(requestParam.getValidDate());
            }
            insertShortLinkDO.setDescribe(requestParam.getDescribe());
            baseMapper.insert(insertShortLinkDO);
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            shortlinkStatistic(fullShortUrl, null, request, response);
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
                shortlinkStatistic(fullShortUrl, null, request, response);
                ((HttpServletResponse) response).sendRedirect(originalLink);
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
            try {
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortlinkDO.getOriginUrl(),
                        LinkUtil.getShortlinkCacheValidTime(shortlinkDO.getValidDate()), TimeUnit.MICROSECONDS
                );
                shortlinkStatistic(fullShortUrl, shortlinkDO.getGid(), request, response);
                ((HttpServletResponse) response).sendRedirect(shortlinkDO.getOriginUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    private void shortlinkStatistic(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicBoolean uvFlag = new AtomicBoolean();
        try {
            Runnable addResponseCookie = () -> {     //请求不携带cookie，在响应中添加cookie，下次请求就会携带cookie
                String cookieUUID = UUID.fastUUID().toString();
                Cookie uvCookie = new Cookie("uv", cookieUUID);
                uvCookie.setMaxAge(30 * 24 * 60 * 60);
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse) response).addCookie(uvCookie);
                uvFlag.set(true);
                stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, cookieUUID);

            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(cookie -> Objects.equals(cookie.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse((cookieUUID -> {     //请求携带cookie，cookie加入redis的集合
                            // 集合中存在加入失败，代表是老用户，uvFlag为false，集合中不存在加入成功，代表是新用户，uvFlag为true
                            Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, cookieUUID);
                            uvFlag.set(uvAdded != null && uvAdded > 0L);
                        }), addResponseCookie);
            } else {
                addResponseCookie.run();
            }
            String remoteAddr = request.getRemoteAddr();
            Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
            boolean uipFlag = uipAdded != null && uipAdded > 0L;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortlinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkGotoDO.class)
                        .eq(ShortlinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortlinkGotoDO shortlinkGotoDO = shortlinkGotoMapper.selectOne(queryWrapper);
                gid = shortlinkGotoDO.getGid();
            }

            int hour = DateUtil.hour(new Date(), true);
            int week = DateUtil.dayOfWeekEnum(new Date()).getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = new LinkAccessStatsDO();
            linkAccessStatsDO.setPv(1);
            linkAccessStatsDO.setUv(uvFlag.get() ? 1 : 0);
            linkAccessStatsDO.setUip(uipFlag ? 1 : 0);
            linkAccessStatsDO.setFullShortUrl(fullShortUrl);
            linkAccessStatsDO.setGid(gid);
            linkAccessStatsDO.setHour(hour);
            linkAccessStatsDO.setWeekday(week);
            linkAccessStatsDO.setDate(new Date());
            linkAccessStatusMapper.shortLinkStats(linkAccessStatsDO);

            //该功能需要调用高德的ip定位付费api，模拟实现即可
            Map<String,Object> localParam=new HashMap<>();
            localParam.put("key",statsLocaleAmapKey);
            localParam.put("ip",remoteAddr);
            String resp = HttpUtil.get(AMAP_REMOTE_URL, localParam);
            JSONObject localObj = JSON.parseObject(resp);
            String infoCode=localObj.getString("infocode");
            if(StrUtil.isNotBlank(infoCode)&&StrUtil.equals(infoCode,"10000")){
                String province=localObj.getString("province");
                String city=localObj.getString("city");
                String adCode=localObj.getString("adcode");
                LinkLocalStatsDO linkLocalStatsDO=new LinkLocalStatsDO();
                linkLocalStatsDO.setFullShortUrl(fullShortUrl);
                linkLocalStatsDO.setGid(gid);
                linkLocalStatsDO.setDate(new Date());
                linkLocalStatsDO.setCnt(1);
                linkLocalStatsDO.setCountry("中国");
                linkLocalStatsDO.setProvince(StrUtil.equals(province,"[]")?"unknown":province);
                linkLocalStatsDO.setCity(StrUtil.equals(city,"[]")?"unknown":city);
                linkLocalStatsDO.setAdcode(StrUtil.equals(adCode,"[]")?"unknown":adCode);
                linkLocalStatsMapper.shortlinkLocalStats(linkLocalStatsDO);
            }

        } catch (Throwable e) {
            log.error("短链接访问量统计异常", e);
        }
    }

    /**
     * 根据原始链接生成短链接，防止重复，尝试10次
     */
    private String generateShortUri(String domain, String originUrl) {
        int customGenerateCount = 1;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException(ShortlinkErrorCodeEnum.TRY_GENERATE_ERROR);
            }
            shortUri = HashUtil.hashToBase62(originUrl + UUID.randomUUID());
            if (!shortUriCreateCachePenetrationBloomFilter.contains(domain + "/" + shortUri)) {
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
}
