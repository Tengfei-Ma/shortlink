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
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.service.RecycleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    public IPage<ShortlinkPageRespDTO> pageRecycleBin(ShortlinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortlinkDO> queryWrapper = Wrappers.lambdaQuery(ShortlinkDO.class)
                .eq(ShortlinkDO::getGid, requestParam.getGid())
                .eq(ShortlinkDO::getEnableStatus, 1)
                .eq(ShortlinkDO::getDelFlag, 0)
                .orderByDesc(ShortlinkDO::getCreateTime);
        Page<ShortlinkDO> page = page(requestParam, queryWrapper);
        return page.convert(shortlinkDO -> BeanUtil.toBean(shortlinkDO, ShortlinkPageRespDTO.class));
    }
}
