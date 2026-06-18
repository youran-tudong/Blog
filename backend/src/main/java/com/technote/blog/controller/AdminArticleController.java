package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.ArticleBatchCategoryReq;
import com.technote.blog.model.req.ArticleBatchDeleteReq;
import com.technote.blog.model.req.ArticleBatchVisibilityReq;
import com.technote.blog.model.req.ArticleExportBatchReq;
import com.technote.blog.model.req.ArticleFlagUpdateReq;
import com.technote.blog.model.req.ArticlePageQueryReq;
import com.technote.blog.model.req.ArticleSaveReq;
import com.technote.blog.model.resp.ArticleExportFileResp;
import com.technote.blog.model.resp.ArticleListResp;
import com.technote.blog.model.resp.ArticleResp;
import com.technote.blog.model.resp.ArticleVersionDetailResp;
import com.technote.blog.model.resp.ArticleVersionResp;
import com.technote.blog.service.ArticleExportService;
import com.technote.blog.service.ArticleService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import com.technote.log.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 后台文章管理接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    private final ArticleExportService articleExportService;

    @GetMapping
    public ApiResult<PageResp<ArticleListResp>> pageArticles(@Valid ArticlePageQueryReq req) {
        return ApiResult.success(articleService.pageArticles(req));
    }

    @GetMapping("/{id}")
    public ApiResult<ArticleResp> getArticle(@PathVariable Long id) {
        return ApiResult.success(articleService.getArticle(id));
    }

    @PostMapping
    @OperationLog(module = "文章管理", operation = "新增文章")
    public ApiResult<ArticleResp> createArticle(@Valid @RequestBody ArticleSaveReq req) {
        return ApiResult.success(articleService.createArticle(req));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "文章管理", operation = "修改文章")
    public ApiResult<ArticleResp> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleSaveReq req) {
        return ApiResult.success(articleService.updateArticle(id, req));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "文章管理", operation = "删除文章")
    public ApiResult<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ApiResult.success();
    }

    @PostMapping("/batch/delete")
    @OperationLog(module = "文章管理", operation = "批量删除文章")
    public ApiResult<Void> batchDeleteArticles(@Valid @RequestBody ArticleBatchDeleteReq req) {
        articleService.batchDeleteArticles(req.getArticleIds());
        return ApiResult.success();
    }

    @PutMapping("/batch/visibility")
    @OperationLog(module = "文章管理", operation = "批量切换公开状态")
    public ApiResult<Void> batchUpdateVisibility(@Valid @RequestBody ArticleBatchVisibilityReq req) {
        articleService.batchUpdateVisibility(req.getArticleIds(), req.getVisibility());
        return ApiResult.success();
    }

    @PutMapping("/batch/category")
    @OperationLog(module = "文章管理", operation = "批量修改分类")
    public ApiResult<Void> batchUpdateCategory(@Valid @RequestBody ArticleBatchCategoryReq req) {
        articleService.batchUpdateCategory(req.getArticleIds(), req.getCategoryId());
        return ApiResult.success();
    }

    @GetMapping("/{id}/export")
    @OperationLog(module = "文章管理", operation = "导出文章")
    public ResponseEntity<byte[]> exportArticle(@PathVariable Long id) {
        return buildMarkdownDownload(articleExportService.exportArticle(id));
    }

    @PostMapping("/export")
    @OperationLog(module = "文章管理", operation = "批量导出文章")
    public ResponseEntity<byte[]> exportArticles(@Valid @RequestBody ArticleExportBatchReq req) {
        return buildMarkdownDownload(articleExportService.exportArticles(req.getArticleIds()));
    }

    @GetMapping("/{id}/versions")
    public ApiResult<List<ArticleVersionResp>> listArticleVersions(@PathVariable Long id) {
        return ApiResult.success(articleService.listArticleVersions(id));
    }

    @GetMapping("/{id}/versions/{versionId}")
    public ApiResult<ArticleVersionDetailResp> getArticleVersion(@PathVariable Long id, @PathVariable Long versionId) {
        return ApiResult.success(articleService.getArticleVersion(id, versionId));
    }

    @PostMapping("/{id}/versions/{versionId}/rollback")
    @OperationLog(module = "文章管理", operation = "回退文章版本")
    public ApiResult<ArticleResp> rollbackArticleVersion(@PathVariable Long id, @PathVariable Long versionId) {
        return ApiResult.success(articleService.rollbackArticleVersion(id, versionId));
    }

    @PutMapping("/{id}/top")
    @OperationLog(module = "文章管理", operation = "切换置顶")
    public ApiResult<Void> updateTopFlag(@PathVariable Long id, @Valid @RequestBody ArticleFlagUpdateReq req) {
        articleService.updateTopFlag(id, req);
        return ApiResult.success();
    }

    @PutMapping("/{id}/visibility")
    @OperationLog(module = "文章管理", operation = "切换公开状态")
    public ApiResult<Void> updateVisibility(@PathVariable Long id, @Valid @RequestBody ArticleFlagUpdateReq req) {
        articleService.updateVisibility(id, req);
        return ApiResult.success();
    }

    private ResponseEntity<byte[]> buildMarkdownDownload(ArticleExportFileResp file) {
        byte[] body = file.getContent().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(body);
    }
}
