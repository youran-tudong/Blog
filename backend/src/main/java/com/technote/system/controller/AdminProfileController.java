package com.technote.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.common.api.ApiResult;
import com.technote.log.OperationLog;
import com.technote.system.model.req.PasswordUpdateReq;
import com.technote.system.model.req.ProfileUpdateReq;
import com.technote.system.model.resp.UserInfoResp;
import com.technote.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台当前管理员个人中心接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/profile")
public class AdminProfileController {

    private final SysUserService sysUserService;

    @GetMapping
    public ApiResult<UserInfoResp> getProfile() {
        return ApiResult.success(sysUserService.getCurrentUser());
    }

    @PutMapping
    @OperationLog(module = "个人中心", operation = "更新个人资料")
    public ApiResult<UserInfoResp> updateProfile(@Valid @RequestBody ProfileUpdateReq req) {
        return ApiResult.success(sysUserService.updateCurrentUserProfile(req));
    }

    @PutMapping("/password")
    @OperationLog(module = "个人中心", operation = "修改登录密码")
    public ApiResult<Void> updatePassword(@Valid @RequestBody PasswordUpdateReq req) {
        sysUserService.updateCurrentUserPassword(req);
        return ApiResult.success();
    }
}

