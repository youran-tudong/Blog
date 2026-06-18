package com.technote.common.exception;

/**
 * 业务异常，后续业务层主动抛出的异常统一使用该类型。
 */
public class BaseException extends RuntimeException {

    private final Integer code;

    public BaseException(String message) {
        this(500, message);
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

