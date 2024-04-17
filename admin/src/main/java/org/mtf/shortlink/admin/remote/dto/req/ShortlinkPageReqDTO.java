package org.mtf.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 分页查询短链接请求参数实体
 */
@Data
public class ShortlinkPageReqDTO extends Page<Void> {
    private String gid;
    /**
     * 排序标识
     */
    private String orderTag;
}
