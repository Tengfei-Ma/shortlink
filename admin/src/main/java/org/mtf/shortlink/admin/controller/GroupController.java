package org.mtf.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.common.convention.result.Results;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortlinkGroupSortReqDTO;
import org.mtf.shortlink.admin.dto.resp.ShortlinkGroupRespDTO;
import org.mtf.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping("/api/shortlink/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        groupService.saveGroup(requestParam);
        return Results.success();
    }

    /**
     * 查询短链接分组列表
     */
    @GetMapping("/api/shortlink/v1/group")
    public Result<List<ShortlinkGroupRespDTO>> listGroup(){
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     */
    @PutMapping("/api/shortlink/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam){
        groupService.updateGroup(requestParam);
        return Results.success();
    }
    /**
     * 删除短链接分组
     */
    @DeleteMapping("/api/shortlink/v1/group")
    public Result<Void> deleteGroup(@RequestParam("gid") String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }
    /**
     * 短链接分组排序
     */
    @PostMapping("/api/shortlink/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortlinkGroupSortReqDTO> requestParam){
        groupService.sortGroup(requestParam);
        return Results.success();
    }
}
