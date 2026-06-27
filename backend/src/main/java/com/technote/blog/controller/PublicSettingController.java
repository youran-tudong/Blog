package com.technote.blog.controller;

import com.technote.blog.model.resp.PublicSettingResp;
import com.technote.blog.service.SettingService;
import com.technote.common.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台站点设置查询接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/settings")
public class PublicSettingController {

    private final SettingService settingService;

    @GetMapping
    public ApiResult<PublicSettingResp> getSetting() {
        return ApiResult.success(settingService.getPublicSetting());
    }
}
