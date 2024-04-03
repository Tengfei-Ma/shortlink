package org.mtf.shortlink.admin.dto.resp;

import lombok.Data;
/**
 * 用户为脱敏信息返回实体
 */
@Data
public class UserActualRespDTO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String mail;
}
