package com.technote.system.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求。
 */
@Data
public class LoginReq {

    /**
     * 登录用户名。
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 登录密码。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

