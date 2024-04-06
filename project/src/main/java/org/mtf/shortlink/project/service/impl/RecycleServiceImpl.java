package org.mtf.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dao.mapper.ShortlinkMapper;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;
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
        ShortlinkDO shortlinkDO=new ShortlinkDO();
        shortlinkDO.setEnableStatus(1);
        baseMapper.update(shortlinkDO,updateWrapper);

        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY,requestParam.getFullShortUrl()));
    }
}
