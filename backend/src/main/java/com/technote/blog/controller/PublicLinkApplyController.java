package com.technote.blog.controller;

import com.technote.blog.model.req.LinkApplySubmitReq;
import com.technote.blog.model.resp.LinkApplyResp;
import com.technote.blog.service.LinkApplyService;
import com.technote.common.api.ApiResult;
import com.technote.common.guard.PublicSubmitGuardService;
import com.technote.common.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台友链申请接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/link-applies")
public class PublicLinkApplyController {

    private final LinkApplyService linkApplyService;

    private final RateLimitService rateLimitService;

    private final PublicSubmitGuardService publicSubmitGuardService;

    @PostMapping
    public ApiResult<LinkApplyResp> submitLinkApply(@Valid @RequestBody LinkApplySubmitReq req, HttpServletRequest request) {
        rateLimitService.checkLinkApply(request);
        publicSubmitGuardService.checkLinkApply(
                req.getCaptchaId(),
                req.getCaptchaAnswer(),
                req.getSiteName(),
                req.getDescription());
        return ApiResult.success(linkApplyService.submitLinkApply(req));
    }
}
