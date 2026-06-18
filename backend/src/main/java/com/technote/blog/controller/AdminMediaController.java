package com.technote.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.technote.blog.model.req.MediaPageQueryReq;
import com.technote.blog.model.resp.MediaResp;
import com.technote.blog.service.MediaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台媒体文件接口。
 */
@RestController
@SaCheckLogin
@RequiredArgsConstructor
@RequestMapping("/admin/media")
public class AdminMediaController {

    private final MediaService mediaService;

    @GetMapping
    public ApiResult<PageResp<MediaResp>> pageMedia(@Valid MediaPageQueryReq req) {
        return ApiResult.success(mediaService.pageMedia(req));
    }

    @PostMapping("/images")
    @OperationLog(module = "媒体管理", operation = "上传图片")
    public ApiResult<MediaResp> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResult.success(mediaService.uploadImage(file));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "媒体管理", operation = "删除媒体")
    public ApiResult<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ApiResult.success();
    }
}
