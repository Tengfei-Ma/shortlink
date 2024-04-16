package org.mtf.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接浏览器监控响应参数
 */
@Data
public class ShortLinkStatsBrowserRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 占比
     */
    private Double ratio;
}

