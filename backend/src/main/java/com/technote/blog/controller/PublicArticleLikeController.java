package com.technote.blog.controller;

import com.technote.blog.model.resp.ArticleLikeStatusResp;
import com.technote.blog.service.ArticleLikeService;
import com.technote.common.api.ApiResult;
import com.technote.common.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台文章匿名点赞接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/articles/{slug}/likes")
public class PublicArticleLikeController {

    private static final String VISITOR_KEY_PATTERN = "^[A-Za-z0-9-]{16,64}$";

    private final ArticleLikeService articleLikeService;

    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public ApiResult<ArticleLikeStatusResp> getLikeStatus(
            @PathVariable String slug,
            @Pattern(regexp = VISITOR_KEY_PATTERN, message = "访客标识格式不正确")
            @RequestHeader("X-Visitor-Key") String visitorKey) {
        return ApiResult.success(articleLikeService.getLikeStatus(slug, visitorKey));
    }

    @PostMapping
    public ApiResult<ArticleLikeStatusResp> likeArticle(
            @PathVariable String slug,
            @Pattern(regexp = VISITOR_KEY_PATTERN, message = "访客标识格式不正确")
            @RequestHeader("X-Visitor-Key") String visitorKey,
            HttpServletRequest request) {
        rateLimitService.checkArticleLike(request, visitorKey);
        return ApiResult.success(articleLikeService.likeArticle(slug, visitorKey));
    }

    @DeleteMapping
    public ApiResult<ArticleLikeStatusResp> unlikeArticle(
            @PathVariable String slug,
            @Pattern(regexp = VISITOR_KEY_PATTERN, message = "访客标识格式不正确")
            @RequestHeader("X-Visitor-Key") String visitorKey,
            HttpServletRequest request) {
        rateLimitService.checkArticleLike(request, visitorKey);
        return ApiResult.success(articleLikeService.unlikeArticle(slug, visitorKey));
    }
}
