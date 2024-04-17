package org.mtf.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;

/**
 * 分页查询短链接请求参数实体
 */
@Data
public class ShortlinkPageReqDTO extends Page<ShortlinkDO> {
    private String gid;
    /**
     * 排序标识
     */
    private String orderTag;
}
