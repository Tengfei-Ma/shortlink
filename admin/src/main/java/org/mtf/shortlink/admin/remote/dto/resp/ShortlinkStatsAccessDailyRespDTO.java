package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接基础访问监控响应参数
 */
@Data
public class ShortlinkStatsAccessDailyRespDTO {

    /**
     * 日期
     */
    private String date;

    /**
     * 访问量
     */
    private Integer pv;

    /**
     * 独立访客数
     */
    private Integer uv;

    /**
     * 独立IP数
     */
    private Integer uip;
}
