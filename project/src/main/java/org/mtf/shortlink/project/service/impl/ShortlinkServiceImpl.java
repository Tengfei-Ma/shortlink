package org.mtf.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtf.shortlink.project.common.convention.exception.ClientException;
import org.mtf.shortlink.project.common.convention.exception.ServiceException;
import org.mtf.shortlink.project.common.enums.ShortlinkErrorCodeEnum;
import org.mtf.shortlink.project.common.enums.ValidDateTypeEnum;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dao.entity.ShortlinkGotoDO;
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
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;
import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.LOCK_GOTO_SHORT_LINK_KEY;

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
    @Override
    public ShortlinkCreateRespDTO createShortLink(ShortlinkCreateReqDTO requestParam) {
        ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);
        String shortUri = generateShortUri(requestParam.getDomain(), requestParam.getOriginUrl());
        String fullShortUrl = requestParam.getDomain() + "/" + shortUri;

        shortlinkDO.setShortUri(shortUri);
        shortlinkDO.setFullShortUrl(fullShortUrl);
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
        //TODO 新增的短链接放入缓存？

        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        ShortlinkCreateRespDTO shortlinkCreateRespDTO = new ShortlinkCreateRespDTO();
        shortlinkCreateRespDTO.setGid(requestParam.getGid());
        shortlinkCreateRespDTO.setOriginUrl(requestParam.getOriginUrl());
        shortlinkCreateRespDTO.setFullShortUrl("http://" + fullShortUrl);
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
            if (Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType())){
                insertShortLinkDO.setValidDate(null);
            }
            else{
                insertShortLinkDO.setValidDate(requestParam.getValidDate());
            }
            insertShortLinkDO.setDescribe(requestParam.getDescribe());
            baseMapper.insert(insertShortLinkDO);
        }
    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl=serverName+"/"+shortUri;
        String originalLink= stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl));
        if(StrUtil.isNotBlank(originalLink)){
            try {
                ((HttpServletResponse) response).sendRedirect(originalLink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try{
            originalLink= stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl));
            if(StrUtil.isNotBlank(originalLink)){
                try {
                    ((HttpServletResponse) response).sendRedirect(originalLink);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            LambdaQueryWrapper<ShortlinkGotoDO> gidQueryWrapper = Wrappers.lambdaQuery(ShortlinkGotoDO.class)
                    .eq(ShortlinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortlinkGotoDO shortlinkGotoDO = shortlinkGotoMapper.selectOne(gidQueryWrapper);
            if(shortlinkGotoDO==null){
                return;
            }
            LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                    .eq(ShortlinkDO::getGid, shortlinkGotoDO.getGid())
                    .eq(ShortlinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortlinkDO::getEnableStatus,0)
                    .eq(ShortlinkDO::getDelFlag,0);
            ShortlinkDO shortlinkDO = baseMapper.selectOne(queryWrapper);
            if(shortlinkDO==null){
                return;
            }
            try {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl),shortlinkDO.getOriginUrl());
                ((HttpServletResponse)response).sendRedirect(shortlinkDO.getOriginUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        finally {
            lock.unlock();
        }
    }

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
}
