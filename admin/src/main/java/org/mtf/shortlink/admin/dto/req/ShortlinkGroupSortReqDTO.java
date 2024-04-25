package org.mtf.shortlink.admin.dto.req;

import lombok.Data;
/**
 * 短链接分组排序参数
 */
@Data
public class ShortlinkGroupSortReqDTO {
    private String gid;
    private Integer sortOrder;
}
