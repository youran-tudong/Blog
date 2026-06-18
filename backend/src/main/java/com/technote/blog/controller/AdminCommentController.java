package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.AuditReq;
import com.technote.blog.model.resp.CommentResp;
import com.technote.blog.service.CommentService;
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
 * 后台评论审核接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResult<PageResp<CommentResp>> pageComments(@Valid AuditPageQueryReq req) {
        return ApiResult.success(commentService.pageAdminComments(req));
    }

    @PutMapping("/{id}/audit")
    @OperationLog(module = "评论审核", operation = "审核评论")
    public ApiResult<Void> auditComment(@PathVariable Long id, @Valid @RequestBody AuditReq req) {
        commentService.auditComment(id, req);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "评论审核", operation = "删除评论")
    public ApiResult<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResult.success();
    }
}
