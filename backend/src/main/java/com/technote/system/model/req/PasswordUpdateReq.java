package com.technote.system.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前管理员密码修改请求。
 */
@Data
public class PasswordUpdateReq {

    /**
     * 当前密码。
     */
    @NotBlank(message = "当前密码不能为空")
    @Size(max = 64, message = "当前密码不能超过64个字符")
    private String oldPassword;

    /**
     * 新密码，至少包含一个字母和一个数字。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "新密码长度必须在8到64个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "新密码至少需要包含一个字母和一个数字")
    private String newPassword;

    /**
     * 新密码确认。
     */
    @NotBlank(message = "确认密码不能为空")
    @Size(max = 64, message = "确认密码不能超过64个字符")
    private String confirmPassword;
}

