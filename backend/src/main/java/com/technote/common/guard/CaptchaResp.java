package com.technote.common.guard;

import lombok.Data;

/**
 * 公开提交数学验证码响应。
 */
@Data
public class CaptchaResp {

    /**
     * 验证码ID，提交公开表单时原样带回。
     */
    private String captchaId;

    /**
     * 数学题目。
     */
    private String question;

    /**
     * 有效期秒数。
     */
    private Integer expireSeconds;
}
