package com.technote.common.guard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公开提交风控设置保存请求。
 */
@Data
public class PublicSubmitGuardSettingReq {

    /**
     * 是否启用公开提交防护。
     */
    @NotNull(message = "请选择是否启用公开提交防护")
    private Boolean enabled;

    /**
     * 是否启用数学验证码。
     */
    @NotNull(message = "请选择是否启用验证码")
    private Boolean captchaEnabled;

    /**
     * 验证码有效期秒数。
     */
    @NotNull(message = "请填写验证码有效期")
    @Min(value = 60, message = "验证码有效期不能少于60秒")
    @Max(value = 3600, message = "验证码有效期不能超过3600秒")
    private Integer captchaTtlSeconds;

    /**
     * 内存中最多保留的验证码数量。
     */
    @NotNull(message = "请填写验证码最大保留数量")
    @Min(value = 100, message = "验证码最大保留数量不能少于100")
    @Max(value = 20000, message = "验证码最大保留数量不能超过20000")
    private Integer captchaMaxEntries;

    /**
     * 是否启用敏感词过滤。
     */
    @NotNull(message = "请选择是否启用敏感词过滤")
    private Boolean sensitiveWordEnabled;

    /**
     * 敏感词命中提示。
     */
    @Size(max = 120, message = "敏感词提示不能超过120个字符")
    private String sensitiveWordMessage;

    /**
     * 敏感词列表，前端按行提交。
     */
    @Size(max = 4000, message = "敏感词列表不能超过4000个字符")
    private String sensitiveWords;
}
