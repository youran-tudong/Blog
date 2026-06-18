package com.technote.system.model.resp;

import lombok.Data;

/**
 * 当前管理员用户信息。
 */
@Data
public class UserInfoResp {

    /**
     * 用户ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像地址。
     */
    private String avatarUrl;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 个人简介。
     */
    private String bio;

    /**
     * 角色编码。
     */
    private String roleCode;
}

