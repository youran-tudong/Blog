package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 前台站点设置响应。
 */
@Data
public class PublicSettingResp {

    private String siteTitle;

    private String siteDescription;

    private String siteKeywords;

    private String icpNo;

    private String authorName;

    private String authorAvatar;

    private String authorProfile;

    private String announcement;

    private String aboutContent;
}
