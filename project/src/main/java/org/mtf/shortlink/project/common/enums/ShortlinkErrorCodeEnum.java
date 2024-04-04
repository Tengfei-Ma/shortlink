package org.mtf.shortlink.project.common.enums;


import org.mtf.shortlink.project.common.convention.errorcode.IErrorCode;

public enum ShortlinkErrorCodeEnum implements IErrorCode {

    SHORTLINK_NOT_EXIST("B000400","短链接不存在"),
    TRY_GENERATE_ERROR("B000401","尝试生成短链接失败"),
    SHORTLINK_EXIST("B000402","短链接已存在");


    private final String code;

    private final String message;

    ShortlinkErrorCodeEnum(String code, String message) {
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
