package org.mtf.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接访问设备监控响应参数
 */
@Data
public class ShortLinkStatsDeviceRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 设备类型
     */
    private String device;

    /**
     * 占比
     */
    private Double ratio;
}
