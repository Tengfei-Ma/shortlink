package org.mtf.shortlink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.mtf.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;

/**
 * 用户脱敏信息返回实体
 */
@Data
public class UserRespDTO {
    private Long id;
    private String username;
    private String realName;
    /**
     * 手机号脱敏处理
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;
    private String mail;
}
