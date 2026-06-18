package com.technote.blog.model.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台友链申请提交请求。
 */
@Data
public class LinkApplySubmitReq {

    /**
     * 网站名称。
     */
    @NotBlank(message = "网站名称不能为空")
    @Size(max = 100, message = "网站名称不能超过100个字符")
    private String siteName;

    /**
     * 网站地址。
     */
    @NotBlank(message = "网站地址不能为空")
    @Size(max = 255, message = "网站地址不能超过255个字符")
    private String siteUrl;

    /**
     * 网站图标地址。
     */
    @Size(max = 500, message = "网站图标不能超过500个字符")
    private String iconUrl;

    /**
     * 网站描述。
     */
    @Size(max = 255, message = "网站描述不能超过255个字符")
    private String description;

    /**
     * 申请人联系邮箱。
     */
    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 128, message = "联系邮箱不能超过128个字符")
    private String applicantEmail;

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
