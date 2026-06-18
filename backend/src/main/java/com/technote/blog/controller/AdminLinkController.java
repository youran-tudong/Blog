package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.LinkSaveReq;
import com.technote.blog.model.resp.LinkResp;
import com.technote.blog.service.LinkService;
import com.technote.common.api.ApiResult;
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
 * 后台友链管理接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/links")
public class AdminLinkController {

    private final LinkService linkService;

    @GetMapping
    public ApiResult<List<LinkResp>> listLinks() {
        return ApiResult.success(linkService.listAdminLinks());
    }

    @PostMapping
    @OperationLog(module = "友链管理", operation = "新增友链")
    public ApiResult<LinkResp> createLink(@Valid @RequestBody LinkSaveReq req) {
        return ApiResult.success(linkService.createLink(req));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "友链管理", operation = "修改友链")
    public ApiResult<LinkResp> updateLink(@PathVariable Long id, @Valid @RequestBody LinkSaveReq req) {
        return ApiResult.success(linkService.updateLink(id, req));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "友链管理", operation = "删除友链")
    public ApiResult<Void> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        return ApiResult.success();
    }
}
