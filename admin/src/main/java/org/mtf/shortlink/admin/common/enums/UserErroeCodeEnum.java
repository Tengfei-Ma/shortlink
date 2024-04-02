package org.mtf.shortlink.admin.common.enums;

import org.mtf.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErroeCodeEnum implements IErrorCode {

    USER_NULL("B000200","用户记录不存在"),
    USER_NAME_EXIST("B000201","用户名已存在"),
    User_EXIST("B000202","用户记录已存在"),
    USER_SAVE_ERROR("B000203","用户系新增失败");

    private final String code;

    private final String message;

    UserErroeCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
