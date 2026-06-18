package com.technote.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.common.api.ApiResult;
import com.technote.dashboard.model.resp.DashboardStatsResp;
import com.technote.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台仪表盘接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResult<DashboardStatsResp> getStats() {
        return ApiResult.success(dashboardService.getStats());
    }
}
