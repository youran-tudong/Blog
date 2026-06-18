package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.LinkApplyAuditReq;
import com.technote.blog.model.resp.LinkApplyResp;
import com.technote.blog.service.LinkApplyService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import com.technote.log.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台友链申请审核接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/link-applies")
public class AdminLinkApplyController {

    private final LinkApplyService linkApplyService;

    @GetMapping
    public ApiResult<PageResp<LinkApplyResp>> pageLinkApplies(@Valid AuditPageQueryReq req) {
        return ApiResult.success(linkApplyService.pageAdminLinkApplies(req));
    }

    @PutMapping("/{id}/audit")
    @OperationLog(module = "友链申请", operation = "审核友链申请")
    public ApiResult<Void> auditLinkApply(@PathVariable Long id, @Valid @RequestBody LinkApplyAuditReq req) {
        linkApplyService.auditLinkApply(id, req);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "友链申请", operation = "删除友链申请")
    public ApiResult<Void> deleteLinkApply(@PathVariable Long id) {
        linkApplyService.deleteLinkApply(id);
        return ApiResult.success();
    }
}
