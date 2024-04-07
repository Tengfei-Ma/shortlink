package org.mtf.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 回收站恢复请求参数实体
 */
@Data
public class RecycleBinRecoverReqDTO {
    private String gid;
    private String fullShortUrl;
}
