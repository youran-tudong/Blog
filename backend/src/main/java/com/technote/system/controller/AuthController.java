package com.technote.system.controller;

import com.technote.common.api.ApiResult;
import com.technote.log.OperationLog;
import com.technote.system.model.req.LoginReq;
import com.technote.system.model.resp.LoginResp;
import com.technote.system.model.resp.UserInfoResp;
import com.technote.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员认证接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    @OperationLog(module = "认证", operation = "管理员登录")
    public ApiResult<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return ApiResult.success(sysUserService.login(req));
    }

    @GetMapping("/me")
    public ApiResult<UserInfoResp> me() {
        return ApiResult.success(sysUserService.getCurrentUser());
    }

    @PostMapping("/logout")
    @OperationLog(module = "认证", operation = "管理员退出")
    public ApiResult<Void> logout() {
        sysUserService.logout();
        return ApiResult.success();
    }
}

