package org.mtf.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.remote.ShortlinkRemoteService;
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
    ShortlinkRemoteService shortlinkRemoteService=new ShortlinkRemoteService() {
    };
    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats")
    public Result<ShortlinkStatsRespDTO> shortlinkStats(ShortlinkStatsReqDTO requestParam) {
        return shortlinkRemoteService.oneShortlinkStats(requestParam);
    }
    /**
     * 查询单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record")
    public Result<IPage<ShortlinkStatsAccessRecordRespDTO>> shortlinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortlinkRemoteService.shortlinkAccessRecord(requestParam);
    }
    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/group")
    public Result<ShortlinkStatsRespDTO> groupShortlinkStats(ShortlinkGroupStatsReqDTO requestParam) {
        return shortlinkRemoteService.groupShortlinkStats(requestParam);
    }
    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record/group")
    public Result<IPage<ShortlinkStatsAccessRecordRespDTO>> groupShortlinkStatsAccessRecord(ShortlinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortlinkRemoteService.groupShortlinkStatsAccessRecord(requestParam);
    }
}
