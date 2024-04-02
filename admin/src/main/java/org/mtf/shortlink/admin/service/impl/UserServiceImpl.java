package org.mtf.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.exception.ClientException;
import org.mtf.shortlink.admin.common.enums.UserErroeCodeEnum;
import org.mtf.shortlink.admin.dao.entity.UserDO;
import org.mtf.shortlink.admin.dao.mapper.UserMapper;
import org.mtf.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.mtf.shortlink.admin.dto.resp.UserRespDTO;
import org.mtf.shortlink.admin.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static org.mtf.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErroeCodeEnum.USER_NULL);
        }
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(userDO, userRespDTO);
        return userRespDTO;
    }

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if(!hasUsername(requestParam.getUsername())){
            throw new ClientException(UserErroeCodeEnum.USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY);
        if(!lock.tryLock()){
            throw new ClientException(UserErroeCodeEnum.USER_NAME_EXIST);
        }
        try{
            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if(inserted<1){
                throw new ClientException(UserErroeCodeEnum.USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        }catch (DuplicateKeyException exception){
            throw new ClientException(UserErroeCodeEnum.User_EXIST);
        }finally {
            lock.unlock();
        }
    }
}
