package com.technote.common.guard;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.common.api.ApiResult;
import com.technote.log.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台公开提交风控设置接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/public-submit-guard")
public class AdminPublicSubmitGuardController {

    private final PublicSubmitGuardService publicSubmitGuardService;

    @GetMapping
    public ApiResult<PublicSubmitGuardSettingResp> getSetting() {
        return ApiResult.success(publicSubmitGuardService.getSetting());
    }

    @PutMapping
    @OperationLog(module = "公开风控", operation = "保存公开提交风控设置")
    public ApiResult<PublicSubmitGuardSettingResp> saveSetting(@Valid @RequestBody PublicSubmitGuardSettingReq req) {
        return ApiResult.success(publicSubmitGuardService.saveSetting(req));
    }
}
