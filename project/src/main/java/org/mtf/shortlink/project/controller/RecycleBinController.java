package org.mtf.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import org.mtf.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkRecycleBinPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.service.RecycleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    private final RecycleService recycleService;

    /**
     * 短链接移至回收站
     */
    @PostMapping("/api/shortlink/v1/recycle-bin")
    public Result<Void> createRecycleBin(@RequestBody RecycleBinCreateReqDTO requestParam){
        recycleService.createRecycleBin(requestParam);
        return Results.success();
    }
    /**
     * 分页查询回收站记录
     */
    @GetMapping("/api/shortlink/v1/recycle-bin/page")
    public Result<IPage<ShortlinkPageRespDTO>> pageShortlink(ShortlinkRecycleBinPageReqDTO requestParam){
        return Results.success(recycleService.pageRecycleBin(requestParam));
    }
    /**
     * 短链接从回收站恢复
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam){
        recycleService.recoverRecycleBin(requestParam);
        return Results.success();
    }
    /**
     * 短链接从回收站彻底删除
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam){
        recycleService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
