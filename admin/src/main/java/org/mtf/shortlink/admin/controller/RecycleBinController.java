package org.mtf.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.common.convention.result.Results;
import org.mtf.shortlink.admin.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import org.mtf.shortlink.admin.remote.ShortlinkRemoteService;
import org.mtf.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.admin.service.RecycleBinService;
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
    ShortlinkRemoteService shortlinkRemoteService=new ShortlinkRemoteService() {
    };
    private final RecycleBinService recycleBinService;

    /**
     * 短链接移至回收站
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin")
    public Result<Void> createRecycleBin(@RequestBody RecycleBinCreateReqDTO requestParam){
        shortlinkRemoteService.createRecycleBin(requestParam);
        return Results.success();
    }
    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/shortlink/admin/v1/recycle-bin/page")
    public Result<IPage<ShortlinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam){
        return recycleBinService.pageRecycleBin(requestParam);
    }
    /**
     * 短链接从回收站恢复
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam){
        shortlinkRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }
}
