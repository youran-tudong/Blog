package com.technote.blog.service;

import com.technote.blog.model.req.SettingSaveReq;
import com.technote.blog.model.resp.PublicSettingResp;
import com.technote.blog.model.resp.SettingResp;

/**
 * 系统设置服务。
 */
public interface SettingService {

    /**
     * 查询系统设置。
     *
     * @return 系统设置
     */
    SettingResp getSetting();

    /**
     * 查询前台站点设置。
     *
     * @return 前台站点设置
     */
    PublicSettingResp getPublicSetting();

    /**
     * 保存系统设置。存在设置记录时更新，不存在时新增。
     *
     * @param req 设置保存请求
     * @return 系统设置
     */
    SettingResp saveSetting(SettingSaveReq req);
}
