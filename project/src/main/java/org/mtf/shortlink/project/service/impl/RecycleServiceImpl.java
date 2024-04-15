package org.mtf.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dao.mapper.ShortlinkMapper;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import org.mtf.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import org.mtf.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.service.RecycleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static org.mtf.shortlink.project.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleServiceImpl extends ServiceImpl<ShortlinkMapper, ShortlinkDO> implements RecycleService {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void createRecycleBin(RecycleBinCreateReqDTO requestParam) {
        LambdaUpdateWrapper<ShortlinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getGid())
                .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortlinkDO::getEnableStatus, 0)
                .eq(ShortlinkDO::getDelFlag, 0);
        ShortlinkDO shortlinkDO = new ShortlinkDO();
        shortlinkDO.setEnableStatus(1);
        baseMapper.update(shortlinkDO, updateWrapper);
        //逻辑删除后将缓存中的短链接跳转移除
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    @Override
    public IPage<ShortlinkPageRespDTO> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                .in(ShortlinkDO::getGid, requestParam.getGidList())
                .eq(ShortlinkDO::getEnableStatus, 1)
                .eq(ShortlinkDO::getDelFlag, 0)
                .orderByDesc(ShortlinkDO::getUpdateTime);
        Page<ShortlinkDO> page = baseMapper.selectPage(requestParam, queryWrapper);
        return page.convert(shortlinkDO -> BeanUtil.toBean(shortlinkDO, ShortlinkPageRespDTO.class));
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<ShortlinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getGid())
                .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortlinkDO::getEnableStatus, 1)
                .eq(ShortlinkDO::getDelFlag, 0);
        ShortlinkDO shortlinkDO = new ShortlinkDO();
        shortlinkDO.setEnableStatus(0);
        baseMapper.update(shortlinkDO, updateWrapper);
        //由于获取原始链接也需要查询数据库，因此缓存预热在短链接跳转时做
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY,requestParam.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortlinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getGid())
                .eq(ShortlinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortlinkDO::getEnableStatus, 1)
                .eq(ShortlinkDO::getDelFlag, 0);
        ShortlinkDO shortlinkDO = new ShortlinkDO();
        shortlinkDO.setDelFlag(1);
        baseMapper.update(shortlinkDO,updateWrapper);
    }
}
