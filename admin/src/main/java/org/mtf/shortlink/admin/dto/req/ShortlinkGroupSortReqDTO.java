package org.mtf.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class ShortlinkGroupSortReqDTO {
    private String gid;
    private Integer sortOrder;
}
