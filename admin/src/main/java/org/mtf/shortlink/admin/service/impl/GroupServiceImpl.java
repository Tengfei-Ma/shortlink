package org.mtf.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mtf.shortlink.admin.common.biz.user.UserContext;
import org.mtf.shortlink.admin.dao.entity.GroupDO;
import org.mtf.shortlink.admin.dao.mapper.GroupMapper;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortlinkGroupSortReqDTO;
import org.mtf.shortlink.admin.dto.resp.ShortlinkGroupRespDTO;
import org.mtf.shortlink.admin.service.GroupService;
import org.mtf.shortlink.admin.toolkit.RandomGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接分组接口实现层
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void saveGroup(ShortLinkGroupSaveReqDTO requestParam) {
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (!hasGid(gid));
        GroupDO groupDO = new GroupDO();
        groupDO.setGid(RandomGenerator.generateRandom());
        groupDO.setName(requestParam.getName());
        groupDO.setUsername(UserContext.getUsername());
        groupDO.setSortOrder(0);
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortlinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder)
                .orderByDesc(GroupDO::getUpdateTime);
        List<GroupDO> groups = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groups, ShortlinkGroupRespDTO.class);
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

    private boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }
}
