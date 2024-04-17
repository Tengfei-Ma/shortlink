package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接地区监控响应参数
 */
@Data
public class ShortlinkStatsLocalCNRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 地区
     */
    private String local;

    /**
     * 占比
     */
    private Double ratio;
}
