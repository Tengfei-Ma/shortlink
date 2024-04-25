package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;
/**
 * 短链接创建参数
 */
@Data
public class ShortlinkCreateRespDTO {
    private String fullShortUrl;
    private String originUrl;
    private String gid;
}
