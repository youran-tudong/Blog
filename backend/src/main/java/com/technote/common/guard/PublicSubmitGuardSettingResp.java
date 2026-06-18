package com.technote.common.guard;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开提交风控设置响应。
 */
@Data
public class PublicSubmitGuardSettingResp {

    private Long id;

    private Boolean enabled;

    private Boolean captchaEnabled;

    private Integer captchaTtlSeconds;

    private Integer captchaMaxEntries;

    private Boolean sensitiveWordEnabled;

    private String sensitiveWordMessage;

    private String sensitiveWords;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
