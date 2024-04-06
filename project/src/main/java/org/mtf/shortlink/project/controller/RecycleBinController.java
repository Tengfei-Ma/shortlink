package org.mtf.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
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
    public Result<IPage<ShortlinkPageRespDTO>> pageShortlink(ShortLinkRecycleBinPageReqDTO requestParam){
        return Results.success(recycleService.pageRecycleBin(requestParam));
    }
}
