package com.technote.blog.model.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台留言提交请求。
 */
@Data
public class GuestbookSubmitReq {

    /**
     * 访客昵称。
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;

    /**
     * 访客邮箱。
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    private String email;

    /**
     * 留言内容。
     */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过1000个字符")
    private String content;

    /**
     * 公开提交验证码ID。
     */
    @Size(max = 64, message = "验证码ID不能超过64个字符")
    private String captchaId;

    /**
     * 公开提交验证码答案。
     */
    @Size(max = 12, message = "验证码答案不能超过12个字符")
    private String captchaAnswer;
}
