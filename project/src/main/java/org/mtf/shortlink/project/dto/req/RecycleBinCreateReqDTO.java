package org.mtf.shortlink.project.dto.req;

import lombok.Data;

/**
 * 移至回收站请求参数实体
 */
@Data
public class RecycleBinCreateReqDTO {
    private String gid;
    private String fullShortUrl;
}
