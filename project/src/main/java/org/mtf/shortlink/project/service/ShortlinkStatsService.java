package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mtf.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortlinkStatsService{
    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 短链接监控数据请求参数
     * @return 短链接监控数据响应参数
     */
    ShortlinkStatsRespDTO oneShortlinkStats(ShortlinkStatsReqDTO requestParam);

    /**
     * 获取单个短链接访问记录
     * @param requestParam 短链接访问记录请求参数
     * @return 短链接访问记录响应参数
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortlinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
}
