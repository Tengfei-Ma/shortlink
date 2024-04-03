package org.mtf.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.mtf.shortlink.admin.common.database.BaseDO;

/**
 * 短链接分组持久层实体
 */

@TableName("t_group")
@Data
public class GroupDO extends BaseDO {
    private Long id;
    private String gid;
    private String name;
    private String username;

}
