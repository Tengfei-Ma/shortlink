package org.mtf.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;
import org.mtf.shortlink.project.service.ShortlinkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortlinkController {
    private final ShortlinkService shortlinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/v1/create")
    public Result<ShortlinkCreateRespDTO> createShortLink(@RequestBody ShortlinkCreateReqDTO requestParam){
        return Results.success(shortlinkService.createShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/v1/page")
    public Result<IPage<ShortlinkPageRespDTO>> pageShortlink(ShortlinkPageReqDTO requestParam){
        return Results.success(shortlinkService.pageShortlink(requestParam));
    }
}
