package org.mtf.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkStatsRespDTO;
import org.mtf.shortlink.project.service.ShortlinkStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortlinkStatsController {
    private final ShortlinkStatsService shortlinkStatsService;

    /**
     * 查询单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/v1/stats")
    public Result<ShortlinkStatsRespDTO> shortLinkStats(ShortlinkStatsReqDTO requestParam) {
        return Results.success(shortlinkStatsService.oneShortLinkStats(requestParam));
    }
}
