package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mtf.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkGroupStatsAccessRecordReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkGroupStatsReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkStatsAccessRecordRespDTO;
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
    IPage<ShortlinkStatsAccessRecordRespDTO> shortlinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 获取分组短链接监控数据入参
     * @return 分组短链接监控数据
     */
    ShortlinkStatsRespDTO groupShortlinkStats(ShortlinkGroupStatsReqDTO requestParam);
    /**
     * 访问分组短链接指定时间内访问记录监控数据
     *
     * @param requestParam 获取分组短链接监控访问记录数据入参
     * @return 分组访问记录监控数据
     */
    IPage<ShortlinkStatsAccessRecordRespDTO> groupShortlinkStatsAccessRecord(ShortlinkGroupStatsAccessRecordReqDTO requestParam);
}
