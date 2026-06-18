package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.SettingSaveReq;
import com.technote.blog.model.resp.SettingResp;
import com.technote.blog.service.SettingService;
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
 * 后台系统设置接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final SettingService settingService;

    @GetMapping
    public ApiResult<SettingResp> getSetting() {
        return ApiResult.success(settingService.getSetting());
    }

    @PutMapping
    @OperationLog(module = "系统设置", operation = "保存系统设置")
    public ApiResult<SettingResp> saveSetting(@Valid @RequestBody SettingSaveReq req) {
        return ApiResult.success(settingService.saveSetting(req));
    }
}
