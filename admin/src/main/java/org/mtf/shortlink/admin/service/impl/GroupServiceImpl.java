package org.mtf.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.biz.user.UserContext;
import org.mtf.shortlink.admin.common.convention.exception.ClientException;
import org.mtf.shortlink.admin.dao.entity.GroupDO;
import org.mtf.shortlink.admin.dao.mapper.GroupMapper;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortlinkGroupSortReqDTO;
import org.mtf.shortlink.admin.dto.resp.ShortlinkGroupRespDTO;
import org.mtf.shortlink.admin.remote.ShortlinkActualRemoteService;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.admin.service.GroupService;
import org.mtf.shortlink.admin.toolkit.RandomGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.mtf.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;
import static org.mtf.shortlink.admin.common.enums.UserErrorCodeEnum.USER_GROUP_OVER_MAX;

/**
 * 短链接分组接口实现层
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    private final ShortlinkActualRemoteService shortlinkActualRemoteService;
    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    @Override
    public void saveGroup(String name) {
        saveGroup(UserContext.getUsername(), name);
    }

    @Override
    public void saveGroup(String username, String name) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() == groupMaxNum) {
                throw new ClientException(USER_GROUP_OVER_MAX);
            }
            String gid;
            do {
                gid = RandomGenerator.generateRandom();
            } while (!hasGid(username, gid));
            GroupDO groupDO = new GroupDO();
            groupDO.setGid(RandomGenerator.generateRandom());
            groupDO.setName(name);
            groupDO.setUsername(username);
            groupDO.setSortOrder(0);
            baseMapper.insert(groupDO);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ShortlinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder)
                .orderByDesc(GroupDO::getUpdateTime);
        List<GroupDO> groups = baseMapper.selectList(queryWrapper);
        List<ShortlinkGroupRespDTO> shortlinkGroupRespDTOS = BeanUtil.copyToList(groups, ShortlinkGroupRespDTO.class);
        List<String> req = shortlinkGroupRespDTOS.stream().map(ShortlinkGroupRespDTO::getGid).toList();
        List<ShortlinkGroupCountRespDTO> resp = shortlinkActualRemoteService.listGroupShortlinkCount(req).getData();
        shortlinkGroupRespDTOS.forEach(each -> {
            Optional<ShortlinkGroupCountRespDTO> first = resp.stream().filter(item -> each.getGid().equals(item.getGid())).findFirst();
            first.ifPresent(firstItem -> each.setShortlinkCount(firstItem.getShortlinkCount()));
        });
        return shortlinkGroupRespDTOS;
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        baseMapper.update(BeanUtil.toBean(requestParam, GroupDO.class), updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void sortGroup(List<ShortlinkGroupSortReqDTO> requestParam) {
        requestParam.stream()
                .map(shortlinkGroupSortReqDTO -> BeanUtil.toBean(shortlinkGroupSortReqDTO, GroupDO.class))
                .forEach(groupDO -> {
                    LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                            .eq(GroupDO::getGid, groupDO.getGid())
                            .eq(GroupDO::getUsername, UserContext.getUsername())
                            .eq(GroupDO::getDelFlag, 0);
                    baseMapper.update(groupDO, updateWrapper);
                });

    }

    private boolean hasGid(String username, String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, username);
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }
}
