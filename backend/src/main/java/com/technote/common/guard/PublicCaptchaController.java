package com.technote.common.guard;

import com.technote.common.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开提交验证码接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/captcha")
public class PublicCaptchaController {

    private final PublicSubmitGuardService publicSubmitGuardService;

    @GetMapping
    public ApiResult<CaptchaResp> createCaptcha() {
        return ApiResult.success(publicSubmitGuardService.createCaptcha());
    }
}
