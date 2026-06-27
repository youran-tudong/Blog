package com.technote.blog.controller;

import com.technote.blog.model.resp.PublicCategoryResp;
import com.technote.blog.model.resp.PublicTagResp;
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
    public ApiResult<List<PublicCategoryResp>> listVisibleCategories() {
        return ApiResult.success(taxonomyService.listVisibleCategories());
    }

    @GetMapping("/tags")
    public ApiResult<List<PublicTagResp>> listTags() {
        return ApiResult.success(taxonomyService.listTags());
    }
}
