package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接访问设备监控响应参数
 */
@Data
public class ShortlinkStatsDeviceRespDTO {

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
