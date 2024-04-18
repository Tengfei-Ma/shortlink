package org.mtf.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkGroupStatsAccessRecordReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkGroupStatsReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkStatsAccessRecordRespDTO;
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
    public Result<ShortlinkStatsRespDTO> shortlinkStats(ShortlinkStatsReqDTO requestParam) {
        return Results.success(shortlinkStatsService.oneShortlinkStats(requestParam));
    }
    /**
     * 查询单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/v1/stats/access-record")
    public Result<IPage<ShortlinkStatsAccessRecordRespDTO>> shortlinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortlinkStatsService.shortlinkAccessRecord(requestParam));
    }
    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/v1/stats/group")
    public Result<ShortlinkStatsRespDTO> groupShortlinkStats(ShortlinkGroupStatsReqDTO requestParam) {
        return Results.success(shortlinkStatsService.groupShortlinkStats(requestParam));
    }
    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/shortlink/v1/stats/access-record/group")
    public Result<IPage<ShortlinkStatsAccessRecordRespDTO>> groupShortlinkStatsAccessRecord(ShortlinkGroupStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortlinkStatsService.groupShortlinkStatsAccessRecord(requestParam));
    }
}
