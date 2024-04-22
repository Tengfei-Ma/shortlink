package org.mtf.shortlink.admin.common.enums;

import org.mtf.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B000200","用户记录不存在"),
    USER_NAME_EXIST("B000201","用户名已存在"),
    User_EXIST("B000202","用户记录已存在"),
    USER_SAVE_ERROR("B000203","用户系新增失败"),
    USER_PASSWORD_ERROR("B000204","用户名或密码错误"),
    USER_LOGIN_ERROR("B000205","用户登陆错误"),
    USER_NOT_LOGIN("B000206","用户未登录"),
    USER_UPDATE_ERROR("B000207","当前登录用户无法修改"),

    USER_GROUP_NOT_EXIST("B000208","用户无分组信息"),
    USER_GROUP_OVER_MAX("B000209","分组数超出限制");


    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
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
