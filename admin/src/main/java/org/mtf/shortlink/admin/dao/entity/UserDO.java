package org.mtf.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.mtf.shortlink.admin.common.database.BaseDO;

/**
 * 用户持久层实体
 */
@TableName("t_user")
@Data
public class UserDO extends BaseDO {

    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String mail;
    private Long deletionTime;
}
