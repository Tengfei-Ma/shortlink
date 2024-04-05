package org.mtf.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 查询分组内短链接数量响应参数实体
 */
@Data
public class ShortlinkGroupCountRespDTO {
    private String gid;
    /**
     * 分组内短链接数量
     */
    private Integer shortlinkCount;
}
