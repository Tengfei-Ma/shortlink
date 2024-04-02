package org.mtf.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户登录请求参数数据传输对象
 */
@Data
public class UserLoginReqDTO {
    private String username;
    private String password;
}
