package org.mtf.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 回收站彻底删除请求参数实体
 */
@Data
public class RecycleBinRemoveReqDTO {
    private String gid;
    private String fullShortUrl;
}
