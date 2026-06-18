package com.technote.system.model.resp;

import lombok.Data;

/**
 * 管理员登录响应。
 */
@Data
public class LoginResp {

    /**
     * Sa-Token token 名称，默认 Authorization。
     */
    private String tokenName;

    /**
     * 登录 token 值。
     */
    private String tokenValue;

    /**
     * 当前登录用户信息。
     */
    private UserInfoResp userInfo;
}

