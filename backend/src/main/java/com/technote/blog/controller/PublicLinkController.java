package com.technote.blog.controller;

import com.technote.blog.model.resp.PublicLinkResp;
import com.technote.blog.service.LinkService;
import com.technote.common.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台友链查询接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/links")
public class PublicLinkController {

    private final LinkService linkService;

    @GetMapping
    public ApiResult<List<PublicLinkResp>> listVisibleLinks() {
        return ApiResult.success(linkService.listVisibleLinks());
    }
}
