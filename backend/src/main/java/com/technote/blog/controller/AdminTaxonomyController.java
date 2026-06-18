package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.CategorySaveReq;
import com.technote.blog.model.req.SortUpdateReq;
import com.technote.blog.model.req.TagSaveReq;
import com.technote.blog.model.req.TaxonomyPageQueryReq;
import com.technote.blog.model.resp.CategoryResp;
import com.technote.blog.model.resp.TagResp;
import com.technote.blog.service.TaxonomyService;
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

/**
 * 后台分类与标签管理接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/taxonomy")
public class AdminTaxonomyController {

    private final TaxonomyService taxonomyService;

    @GetMapping("/categories")
    public ApiResult<PageResp<CategoryResp>> pageCategories(@Valid TaxonomyPageQueryReq req) {
        return ApiResult.success(taxonomyService.pageCategories(req));
    }

    @PostMapping("/categories")
    @OperationLog(module = "分类管理", operation = "新增分类")
    public ApiResult<CategoryResp> createCategory(@Valid @RequestBody CategorySaveReq req) {
        return ApiResult.success(taxonomyService.createCategory(req));
    }

    @PutMapping("/categories/{id}")
    @OperationLog(module = "分类管理", operation = "修改分类")
    public ApiResult<CategoryResp> updateCategory(@PathVariable Long id, @Valid @RequestBody CategorySaveReq req) {
        return ApiResult.success(taxonomyService.updateCategory(id, req));
    }

    @PutMapping("/categories/{id}/sort")
    @OperationLog(module = "分类管理", operation = "修改分类排序")
    public ApiResult<Void> updateCategorySort(@PathVariable Long id, @Valid @RequestBody SortUpdateReq req) {
        taxonomyService.updateCategorySort(id, req);
        return ApiResult.success();
    }

    @DeleteMapping("/categories/{id}")
    @OperationLog(module = "分类管理", operation = "删除分类")
    public ApiResult<Void> deleteCategory(@PathVariable Long id) {
        taxonomyService.deleteCategory(id);
        return ApiResult.success();
    }

    @GetMapping("/tags")
    public ApiResult<PageResp<TagResp>> pageTags(@Valid TaxonomyPageQueryReq req) {
        return ApiResult.success(taxonomyService.pageTags(req));
    }

    @PostMapping("/tags")
    @OperationLog(module = "标签管理", operation = "新增标签")
    public ApiResult<TagResp> createTag(@Valid @RequestBody TagSaveReq req) {
        return ApiResult.success(taxonomyService.createTag(req));
    }

    @PutMapping("/tags/{id}")
    @OperationLog(module = "标签管理", operation = "修改标签")
    public ApiResult<TagResp> updateTag(@PathVariable Long id, @Valid @RequestBody TagSaveReq req) {
        return ApiResult.success(taxonomyService.updateTag(id, req));
    }

    @DeleteMapping("/tags/{id}")
    @OperationLog(module = "标签管理", operation = "删除标签")
    public ApiResult<Void> deleteTag(@PathVariable Long id) {
        taxonomyService.deleteTag(id);
        return ApiResult.success();
    }
}

