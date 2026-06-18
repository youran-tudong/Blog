package com.technote.blog.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统设置保存请求。
 */
@Data
public class SettingSaveReq {

    /**
     * 站点标题。
     */
    @NotBlank(message = "站点标题不能为空")
    @Size(max = 100, message = "站点标题不能超过100个字符")
    private String siteTitle;

    /**
     * 站点简介。
     */
    @Size(max = 255, message = "站点简介不能超过255个字符")
    private String siteDescription;

    /**
     * 站点关键词。
     */
    @Size(max = 255, message = "站点关键词不能超过255个字符")
    private String siteKeywords;

    /**
     * 备案号。
     */
    @Size(max = 100, message = "备案号不能超过100个字符")
    private String icpNo;

    /**
     * 博主名称。
     */
    @Size(max = 64, message = "博主名称不能超过64个字符")
    private String authorName;

    /**
     * 博主头像。
     */
    @Size(max = 500, message = "博主头像不能超过500个字符")
    private String authorAvatar;

    /**
     * 博主介绍。
     */
    private String authorProfile;

    /**
     * 公告内容。
     */
    @Size(max = 500, message = "公告内容不能超过500个字符")
    private String announcement;

    /**
     * 关于页面内容。
     */
    private String aboutContent;
}
