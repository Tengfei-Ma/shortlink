package org.mtf.shortlink.project.service;

import org.mtf.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortlinkStatsService{
    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortlinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);
}
