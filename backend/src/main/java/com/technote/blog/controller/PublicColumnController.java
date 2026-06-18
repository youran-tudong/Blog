package com.technote.blog.controller;

import com.technote.blog.model.req.PublicArticlePageQueryReq;
import com.technote.blog.model.resp.ArticleListResp;
import com.technote.blog.model.resp.ColumnResp;
import com.technote.blog.service.ArticleService;
import com.technote.blog.service.ColumnService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台专栏查询接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/columns")
public class PublicColumnController {

    private final ColumnService columnService;

    private final ArticleService articleService;

    @GetMapping
    public ApiResult<List<ColumnResp>> listColumns() {
        return ApiResult.success(columnService.listVisibleColumns());
    }

    @GetMapping("/{slug}")
    public ApiResult<ColumnResp> getColumn(@PathVariable String slug) {
        return ApiResult.success(columnService.getPublicColumnBySlug(slug));
    }

    @GetMapping("/{slug}/articles")
    public ApiResult<PageResp<ArticleListResp>> pageColumnArticles(@PathVariable String slug,
                                                                   @Valid PublicArticlePageQueryReq req) {
        ColumnResp column = columnService.getPublicColumnBySlug(slug);
        req.setColumnId(column.getId());
        return ApiResult.success(articleService.pagePublicArticles(req));
    }
}
