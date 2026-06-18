package com.technote.log.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import com.technote.log.model.req.OperationLogPageQueryReq;
import com.technote.log.model.resp.OperationLogResp;
import com.technote.log.service.OperationLogQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台操作日志查询接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/operation-logs")
public class AdminOperationLogController {

    private final OperationLogQueryService operationLogQueryService;

    @GetMapping
    public ApiResult<PageResp<OperationLogResp>> pageLogs(@Valid OperationLogPageQueryReq req) {
        return ApiResult.success(operationLogQueryService.pageLogs(req));
    }
}
