package org.mtf.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
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
     * 短链接跳转长链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response)    {
        shortlinkService.restoreUrl(shortUri,request,response);
    }

    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/v1/link")
    public Result<ShortlinkCreateRespDTO> createShortLink(@RequestBody ShortlinkCreateReqDTO requestParam){
        return Results.success(shortlinkService.createShortLink(requestParam));
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
