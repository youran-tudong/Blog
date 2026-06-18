package com.technote.blog.controller;

import com.technote.blog.model.req.GuestbookSubmitReq;
import com.technote.blog.model.resp.GuestbookResp;
import com.technote.blog.service.GuestbookService;
import com.technote.common.api.ApiResult;
import com.technote.common.guard.PublicSubmitGuardService;
import com.technote.common.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台留言接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/guestbooks")
public class PublicGuestbookController {

    private final GuestbookService guestbookService;

    private final RateLimitService rateLimitService;

    private final PublicSubmitGuardService publicSubmitGuardService;

    @GetMapping
    public ApiResult<List<GuestbookResp>> listApprovedGuestbooks() {
        return ApiResult.success(guestbookService.listApprovedGuestbooks());
    }

    @PostMapping
    public ApiResult<GuestbookResp> submitGuestbook(@Valid @RequestBody GuestbookSubmitReq req, HttpServletRequest request) {
        rateLimitService.checkGuestbook(request);
        publicSubmitGuardService.checkGuestbook(req.getCaptchaId(), req.getCaptchaAnswer(), req.getNickname(), req.getContent());
        return ApiResult.success(guestbookService.submitGuestbook(req));
    }
}
