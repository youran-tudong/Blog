package com.technote.system.controller;

import com.technote.common.api.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口。
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResult<String> health() {
        return ApiResult.success("TechNote backend is ready");
    }
}

