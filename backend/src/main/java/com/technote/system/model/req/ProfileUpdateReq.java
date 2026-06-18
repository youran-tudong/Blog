package com.technote.system.model.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前管理员个人资料更新请求。
 */
@Data
public class ProfileUpdateReq {

    /**
     * 显示昵称。
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;

    /**
     * 头像地址。
     */
    @Size(max = 500, message = "头像地址不能超过500个字符")
    private String avatarUrl;

    /**
     * 联系邮箱。
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    private String email;

    /**
     * 个人简介。
     */
    @Size(max = 1000, message = "个人简介不能超过1000个字符")
    private String bio;
}

