package org.mtf.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组修改参数
 */
@Data
public class ShortlinkGroupUpdateReqDTO {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 分组名
     */
    private String name;
}
