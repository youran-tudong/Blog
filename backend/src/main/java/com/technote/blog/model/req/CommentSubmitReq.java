package com.technote.blog.model.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台评论提交请求。
 */
@Data
public class CommentSubmitReq {

    /**
     * 文章ID。
     */
    @NotNull(message = "文章ID不能为空")
    @Positive(message = "文章ID必须大于0")
    private Long articleId;

    /**
     * 父评论ID，0表示一级评论。
     */
    @PositiveOrZero(message = "父评论ID不能小于0")
    private Long parentId = 0L;

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
     * 访客网站。
     */
    @Size(max = 255, message = "网站不能超过255个字符")
    private String website;

    /**
     * 评论内容。
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
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
