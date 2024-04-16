package org.mtf.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接访客监控响应参数
 */
@Data
public class ShortlinkStatsUvRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 访客类型
     */
    private String uvType;

    /**
     * 占比
     */
    private Double ratio;
}