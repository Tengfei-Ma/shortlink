package org.mtf.shortlink.admin.dto.resp;

import lombok.Data;
/**
 * 短链接分组信息返回实体
 */
@Data
public class ShortlinkGroupRespDTO {
    private String gid;
    private String name;
    private Integer sortOrder;
    /**
     * 该分组中短链接数
     */
    private Integer shortlinkCount;
}
