package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接高频访问IP监控响应参数
 */
@Data
public class ShortlinkStatsTopIpRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * IP
     */
    private String ip;
}