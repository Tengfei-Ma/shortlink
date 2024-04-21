package org.mtf.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.remote.ShortlinkActualRemoteService;
import org.mtf.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkGroupStatsAccessRecordReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkGroupStatsReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkStatsReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkStatsAccessRecordRespDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkStatsRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController()
@RequiredArgsConstructor
public class ShortlinkStatsController {
    private final ShortlinkActualRemoteService shortlinkActualRemoteService;
    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats")
    public Result<ShortlinkStatsRespDTO> shortlinkStats(ShortlinkStatsReqDTO requestParam) {
        return shortlinkActualRemoteService.oneShortlinkStats(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );
    }
    /**
     * 查询单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record")
    public Result<Page<ShortlinkStatsAccessRecordRespDTO>> shortlinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortlinkActualRemoteService.shortlinkAccessRecord(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }
    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/group")
    public Result<ShortlinkStatsRespDTO> groupShortlinkStats(ShortlinkGroupStatsReqDTO requestParam) {
        return shortlinkActualRemoteService.groupShortlinkStats(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );
    }
    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record/group")
    public Result<Page<ShortlinkStatsAccessRecordRespDTO>> groupShortlinkStatsAccessRecord(ShortlinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortlinkActualRemoteService.groupShortlinkStatsAccessRecord(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }
}
