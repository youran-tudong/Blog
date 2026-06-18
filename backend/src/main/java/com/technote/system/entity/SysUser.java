package com.technote.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户实体，对应 sys_user 表。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * BCrypt 加密密码。
     */
    private String password;

    /**
     * 显示昵称。
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
     * 角色编码：ADMIN 管理员。
     */
    private String roleCode;

    /**
     * 状态：0禁用 1启用。
     */
    private Integer status;

    /**
     * 最后登录时间。
     */
    private LocalDateTime lastLoginTime;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除 1已删除。
     */
    @TableLogic
    private Integer deleted;
}

