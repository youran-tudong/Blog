package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.ArticleAutoDraftCleanupReq;
import com.technote.blog.model.req.ArticleAutoDraftPageQueryReq;
import com.technote.blog.model.req.ArticleAutoDraftSaveReq;
import com.technote.blog.model.resp.ArticleAutoDraftCleanupResp;
import com.technote.blog.model.resp.ArticleAutoDraftListResp;
import com.technote.blog.model.resp.ArticleAutoDraftResp;
import com.technote.blog.service.ArticleAutoDraftService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import com.technote.log.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台文章自动草稿接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/article-auto-drafts")
public class AdminArticleAutoDraftController {

    private final ArticleAutoDraftService autoDraftService;

    @GetMapping
    public ApiResult<PageResp<ArticleAutoDraftListResp>> pageAutoDrafts(@Valid ArticleAutoDraftPageQueryReq req) {
        return ApiResult.success(autoDraftService.pageAutoDrafts(req));
    }

    @PostMapping
    public ApiResult<ArticleAutoDraftResp> saveAutoDraft(@Valid @RequestBody ArticleAutoDraftSaveReq req) {
        return ApiResult.success(autoDraftService.saveAutoDraft(req));
    }

    @PostMapping("/cleanup")
    @OperationLog(module = "自动草稿", operation = "清理过期自动草稿")
    public ApiResult<ArticleAutoDraftCleanupResp> cleanupExpiredAutoDrafts(@Valid @RequestBody ArticleAutoDraftCleanupReq req) {
        return ApiResult.success(autoDraftService.cleanupExpiredAutoDrafts(req));
    }

    @GetMapping("/{draftKey}")
    public ApiResult<ArticleAutoDraftResp> getAutoDraftByKey(@PathVariable String draftKey) {
        return ApiResult.success(autoDraftService.getAutoDraftByKey(draftKey));
    }

    @GetMapping("/article/{articleId}")
    public ApiResult<ArticleAutoDraftResp> getAutoDraftByArticle(@PathVariable Long articleId) {
        return ApiResult.success(autoDraftService.getAutoDraftByArticle(articleId));
    }

    @DeleteMapping("/{draftKey}")
    public ApiResult<Void> deleteAutoDraftByKey(@PathVariable String draftKey) {
        autoDraftService.deleteAutoDraftByKey(draftKey);
        return ApiResult.success();
    }

    @DeleteMapping("/id/{id}")
    @OperationLog(module = "自动草稿", operation = "删除自动草稿")
    public ApiResult<Void> deleteAutoDraftById(@PathVariable Long id) {
        autoDraftService.deleteAutoDraftById(id);
        return ApiResult.success();
    }
}
