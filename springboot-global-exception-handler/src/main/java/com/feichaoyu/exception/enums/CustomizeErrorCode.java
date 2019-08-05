package com.feichaoyu.exception.enums;

import com.feichaoyu.exception.exceptions.ICustomizeErrorCode;

/**
 * @Author feichaoyu
 * @Date 2019/8/5
 */
public enum CustomizeErrorCode implements ICustomizeErrorCode {

    MY_ERROR(1000, "自定义错误"),
    ;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    private Integer code;
    private String message;

    CustomizeErrorCode(Integer code, String message) {
        this.message = message;
        this.code = code;
    }
}
