package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.RecyclePageQueryReq;
import com.technote.blog.model.resp.RecycleResp;
import com.technote.blog.service.RecycleService;
import com.technote.common.api.ApiResult;
import com.technote.common.model.PageResp;
import com.technote.log.OperationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台回收站接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/recycle")
public class AdminRecycleController {

    private final RecycleService recycleService;

    @GetMapping
    public ApiResult<PageResp<RecycleResp>> pageRecycle(@Valid RecyclePageQueryReq req) {
        return ApiResult.success(recycleService.pageRecycle(req));
    }

    @PostMapping("/{id}/restore")
    @OperationLog(module = "回收站", operation = "恢复资源")
    public ApiResult<Void> restoreRecycle(@PathVariable Long id) {
        recycleService.restoreRecycle(id);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "回收站", operation = "永久删除资源")
    public ApiResult<Void> deleteRecyclePermanently(@PathVariable Long id) {
        recycleService.deleteRecyclePermanently(id);
        return ApiResult.success();
    }
}
