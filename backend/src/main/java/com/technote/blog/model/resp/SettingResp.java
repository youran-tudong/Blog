package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置响应。
 */
@Data
public class SettingResp {

    private Long id;

    private String siteTitle;

    private String siteDescription;

    private String siteKeywords;

    private String icpNo;

    private String authorName;

    private String authorAvatar;

    private String authorProfile;

    private String announcement;

    private String aboutContent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
