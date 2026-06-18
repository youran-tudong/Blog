package com.technote.common.api;

import lombok.Data;

/**
 * 统一接口返回结果。
 *
 * @param <T> 返回数据类型
 */
@Data
public class ApiResult<T> {

    private Integer code;

    private String message;

    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> fail(Integer code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}

