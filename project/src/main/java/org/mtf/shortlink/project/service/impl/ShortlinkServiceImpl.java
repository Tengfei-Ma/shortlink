package org.mtf.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mtf.shortlink.project.common.convention.exception.ServiceException;
import org.mtf.shortlink.project.common.enums.ShortlinkErrorCodeEnum;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dao.mapper.ShortlinkMapper;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.service.ShortlinkService;
import org.mtf.shortlink.project.toolkit.HashUtil;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortlinkServiceImpl extends ServiceImpl<ShortlinkMapper, ShortlinkDO> implements ShortlinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortlinkCreateRespDTO createShortLink(ShortlinkCreateReqDTO requestParam) {
        ShortlinkDO shortlinkDO = BeanUtil.toBean(requestParam, ShortlinkDO.class);
        String shortUri = generateShortUri(requestParam.getDomain(), requestParam.getOriginUrl());
        String fullShortUrl = requestParam.getDomain() + "/" + shortUri;

        shortlinkDO.setShortUri(shortUri);
        shortlinkDO.setFullShortUrl(fullShortUrl);
        shortlinkDO.setClickNum(0);
        shortlinkDO.setEnableStatus(0);
        try {
            baseMapper.insert(shortlinkDO);
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
