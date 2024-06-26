package org.mtf.shortlink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.ShortlinkBatchCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkBatchCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.handler.CustomBlockHandler;
import org.mtf.shortlink.project.service.ShortlinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortlinkController {
    private final ShortlinkService shortlinkService;
    /**
     * 短链接跳转原始链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response)    {
        shortlinkService.restoreUrl(shortUri,request,response);
    }

    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/v1/link")
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortlinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortlinkCreateRespDTO> createShortlink(@RequestBody ShortlinkCreateReqDTO requestParam){
        return Results.success(shortlinkService.createShortLink(requestParam));
    }
    /**
     * 通过分布式锁创建短链接
     */
    @PostMapping("/api/shortlink/v1/create/by-lock")
    public Result<ShortlinkCreateRespDTO> createShortLinkByLock(@RequestBody ShortlinkCreateReqDTO requestParam) {
        return Results.success(shortlinkService.createShortlinkByLock(requestParam));
    }
    /**
     * 批量创建短链接
     */
    @PostMapping("/api/shortlink/v1/link/batch")
    public Result<ShortlinkBatchCreateRespDTO> batchCreateShortlink(@RequestBody ShortlinkBatchCreateReqDTO requestParam) {
        return Results.success(shortlinkService.batchCreateShortlink(requestParam));
    }
    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/v1/link/page")
    public Result<IPage<ShortlinkPageRespDTO>> pageShortlink(ShortlinkPageReqDTO requestParam){
        return Results.success(shortlinkService.pageShortlink(requestParam));
    }
    /**
     * 查询分组内短链接数量
     */
    @GetMapping("/api/shortlink/v1/link/count")
    public Result<List<ShortlinkGroupCountRespDTO>> listGroupShortlinkCount(@RequestParam("requestParam") List<String> requestParam){
        return Results.success(shortlinkService.listGroupShortlinkCount(requestParam));
    }
    /**
     * 修改短链接
     */
    @PutMapping("/api/shortlink/v1/link")
    public Result<Void> updateShortlink(@RequestBody ShortlinkUpdateReqDTO requestParam){
        shortlinkService.updateShortlink(requestParam);
        return Results.success();
    }

}
