package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接访问网络监控响应参数
 */
@Data
public class ShortlinkStatsNetworkRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 占比
     */
    private Double ratio;
}

