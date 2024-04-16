package org.mtf.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接地区监控响应参数
 */
@Data
public class ShortLinkStatsLocaleCNRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 地区
     */
    private String locale;

    /**
     * 占比
     */
    private Double ratio;
}
