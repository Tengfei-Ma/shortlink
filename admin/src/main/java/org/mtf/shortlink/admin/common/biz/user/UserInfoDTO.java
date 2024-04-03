package org.mtf.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class UserInfoDTO {
    @JSONField(name = "id")
    private String userId;
    private String username;
    private String realName;
}
