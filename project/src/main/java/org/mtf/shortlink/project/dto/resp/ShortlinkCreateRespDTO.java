package org.mtf.shortlink.project.dto.resp;

import lombok.Data;
/**
 * 短链接创建响应对象
 */
@Data
public class ShortlinkCreateRespDTO {
    private String fullShortUrl;
    private String originUrl;
    private String gid;
}
