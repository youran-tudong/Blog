package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.ColumnArticleSaveReq;
import com.technote.blog.model.req.ColumnPageQueryReq;
import com.technote.blog.model.req.ColumnSaveReq;
import com.technote.blog.model.req.SortUpdateReq;
import com.technote.blog.model.resp.ColumnArticleResp;
import com.technote.blog.model.resp.ColumnResp;
import com.technote.blog.service.ColumnService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import com.technote.log.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台专栏管理接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/columns")
public class AdminColumnController {

    private final ColumnService columnService;

    @GetMapping
    public ApiResult<PageResp<ColumnResp>> pageColumns(@Valid ColumnPageQueryReq req) {
        return ApiResult.success(columnService.pageColumns(req));
    }

    @PostMapping
    @OperationLog(module = "专栏管理", operation = "新增专栏")
    public ApiResult<ColumnResp> createColumn(@Valid @RequestBody ColumnSaveReq req) {
        return ApiResult.success(columnService.createColumn(req));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "专栏管理", operation = "修改专栏")
    public ApiResult<ColumnResp> updateColumn(@PathVariable Long id, @Valid @RequestBody ColumnSaveReq req) {
        return ApiResult.success(columnService.updateColumn(id, req));
    }

    @PutMapping("/{id}/sort")
    @OperationLog(module = "专栏管理", operation = "修改专栏排序")
    public ApiResult<Void> updateColumnSort(@PathVariable Long id, @Valid @RequestBody SortUpdateReq req) {
        columnService.updateColumnSort(id, req);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专栏管理", operation = "删除专栏")
    public ApiResult<Void> deleteColumn(@PathVariable Long id) {
        columnService.deleteColumn(id);
        return ApiResult.success();
    }

    @GetMapping("/{id}/articles")
    public ApiResult<List<ColumnArticleResp>> listColumnArticles(@PathVariable Long id) {
        return ApiResult.success(columnService.listColumnArticles(id));
    }

    @PostMapping("/{id}/articles")
    @OperationLog(module = "专栏管理", operation = "添加专栏文章")
    public ApiResult<Void> addArticleToColumn(@PathVariable Long id, @Valid @RequestBody ColumnArticleSaveReq req) {
        columnService.addArticleToColumn(id, req);
        return ApiResult.success();
    }

    @PutMapping("/{id}/articles/{articleId}/sort")
    @OperationLog(module = "专栏管理", operation = "修改专栏文章排序")
    public ApiResult<Void> updateColumnArticleSort(@PathVariable Long id,
                                                   @PathVariable Long articleId,
                                                   @Valid @RequestBody SortUpdateReq req) {
        columnService.updateColumnArticleSort(id, articleId, req);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}/articles/{articleId}")
    @OperationLog(module = "专栏管理", operation = "移除专栏文章")
    public ApiResult<Void> removeArticleFromColumn(@PathVariable Long id, @PathVariable Long articleId) {
        columnService.removeArticleFromColumn(id, articleId);
        return ApiResult.success();
    }
}
