package org.mtf.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.biz.user.UserContext;
import org.mtf.shortlink.admin.common.convention.exception.ServiceException;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.dao.entity.GroupDO;
import org.mtf.shortlink.admin.dao.mapper.GroupMapper;
import org.mtf.shortlink.admin.remote.ShortlinkActualRemoteService;
import org.mtf.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.admin.service.RecycleBinService;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.mtf.shortlink.admin.common.enums.UserErrorCodeEnum.USER_GROUP_NOT_EXIST;

/**
 * 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {
    private final ShortlinkActualRemoteService shortlinkActualRemoteService;
    private final GroupMapper groupMapper;

    @Override
    public Result<Page<ShortlinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOS = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOS)) {
            throw new ServiceException(USER_GROUP_NOT_EXIST);
        }
        requestParam.setGidList(groupDOS.stream().map(GroupDO::getGid).toList());
        return shortlinkActualRemoteService.pageRecycleBin(requestParam.getGidList(),requestParam.getCurrent(),requestParam.getSize());
    }
}
