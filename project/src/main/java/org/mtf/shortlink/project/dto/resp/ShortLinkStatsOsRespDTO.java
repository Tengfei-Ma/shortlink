package org.mtf.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接操作系统监控响应参数
 */
@Data
public class ShortLinkStatsOsRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 占比
     */
    private Double ratio;
}