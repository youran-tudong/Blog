package com.technote.blog.controller;

import com.technote.blog.model.req.PublicArticlePageQueryReq;
import com.technote.blog.model.req.PublicArticleSearchReq;
import com.technote.blog.model.resp.ArchiveHeatmapResp;
import com.technote.blog.model.resp.ArchiveMonthResp;
import com.technote.blog.model.resp.ArticleListResp;
import com.technote.blog.model.resp.ArticleResp;
import com.technote.blog.model.resp.ArticleSearchResp;
import com.technote.blog.service.ArticleService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台公开文章查询接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/articles")
public class PublicArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResult<PageResp<ArticleListResp>> pagePublicArticles(@Valid PublicArticlePageQueryReq req) {
        return ApiResult.success(articleService.pagePublicArticles(req));
    }

    @GetMapping("/search")
    public ApiResult<PageResp<ArticleSearchResp>> searchPublicArticles(@Valid PublicArticleSearchReq req) {
        return ApiResult.success(articleService.searchPublicArticles(req));
    }

    @GetMapping("/archives")
    public ApiResult<List<ArchiveMonthResp>> listArchives() {
        return ApiResult.success(articleService.listPublicArchives());
    }

    @GetMapping("/archives/heatmap")
    public ApiResult<ArchiveHeatmapResp> getArchiveHeatmap(@RequestParam(required = false) Integer year) {
        return ApiResult.success(articleService.getPublicArchiveHeatmap(year));
    }

    @GetMapping("/popular")
    public ApiResult<List<ArticleListResp>> listPopularArticles(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return ApiResult.success(articleService.listPopularPublicArticles(limit));
    }

    @GetMapping("/{slug}/related")
    public ApiResult<List<ArticleListResp>> listRelatedArticles(
            @PathVariable String slug,
            @RequestParam(required = false, defaultValue = "4") Integer limit) {
        return ApiResult.success(articleService.listRelatedPublicArticles(slug, limit));
    }

    @GetMapping("/{slug}")
    public ApiResult<ArticleResp> getPublicArticle(@PathVariable String slug) {
        return ApiResult.success(articleService.getPublicArticleBySlug(slug));
    }
}
