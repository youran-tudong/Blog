package com.technote.blog.controller;

import com.technote.blog.model.req.CommentSubmitReq;
import com.technote.blog.model.resp.PublicCommentResp;
import com.technote.blog.service.CommentService;
import com.technote.common.api.ApiResult;
import com.technote.common.guard.PublicSubmitGuardService;
import com.technote.common.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台评论接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/comments")
public class PublicCommentController {

    private final CommentService commentService;

    private final RateLimitService rateLimitService;

    private final PublicSubmitGuardService publicSubmitGuardService;

    @GetMapping
    public ApiResult<List<PublicCommentResp>> listApprovedComments(
            @NotNull(message = "文章ID不能为空") @RequestParam Long articleId) {
        return ApiResult.success(commentService.listApprovedComments(articleId));
    }

    @PostMapping
    public ApiResult<PublicCommentResp> submitComment(@Valid @RequestBody CommentSubmitReq req, HttpServletRequest request) {
        rateLimitService.checkComment(request);
        publicSubmitGuardService.checkComment(req.getCaptchaId(), req.getCaptchaAnswer(), req.getNickname(), req.getContent());
        return ApiResult.success(commentService.submitComment(req));
    }
}
