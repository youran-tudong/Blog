package com.technote.blog.controller;

import com.technote.blog.model.resp.CategoryResp;
import com.technote.blog.model.resp.TagResp;
import com.technote.blog.service.TaxonomyService;
import com.technote.common.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台分类与标签查询接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/taxonomy")
public class PublicTaxonomyController {

    private final TaxonomyService taxonomyService;

    @GetMapping("/categories")
    public ApiResult<List<CategoryResp>> listVisibleCategories() {
        return ApiResult.success(taxonomyService.listVisibleCategories());
    }

    @GetMapping("/tags")
    public ApiResult<List<TagResp>> listTags() {
        return ApiResult.success(taxonomyService.listTags());
    }
}

