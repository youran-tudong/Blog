package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.AuditReq;
import com.technote.blog.model.resp.GuestbookResp;
import com.technote.blog.service.GuestbookService;
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
 * 后台留言审核接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/guestbooks")
public class AdminGuestbookController {

    private final GuestbookService guestbookService;

    @GetMapping
    public ApiResult<PageResp<GuestbookResp>> pageGuestbooks(@Valid AuditPageQueryReq req) {
        return ApiResult.success(guestbookService.pageAdminGuestbooks(req));
    }

    @PutMapping("/{id}/audit")
    @OperationLog(module = "留言审核", operation = "审核留言")
    public ApiResult<Void> auditGuestbook(@PathVariable Long id, @Valid @RequestBody AuditReq req) {
        guestbookService.auditGuestbook(id, req);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "留言审核", operation = "删除留言")
    public ApiResult<Void> deleteGuestbook(@PathVariable Long id) {
        guestbookService.deleteGuestbook(id);
        return ApiResult.success();
    }
}
