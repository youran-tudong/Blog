package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置实体，对应 blog_setting 表。
 */
@Data
@TableName("blog_setting")
public class BlogSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 站点标题。
     */
    private String siteTitle;

    /**
     * 站点简介。
     */
    private String siteDescription;

    /**
     * 站点关键词。
     */
    private String siteKeywords;

    /**
     * 备案号。
     */
    private String icpNo;

    /**
     * 博主名称。
     */
    private String authorName;

    /**
     * 博主头像。
     */
    private String authorAvatar;

    /**
     * 博主介绍。
     */
    private String authorProfile;

    /**
     * 公告内容。
     */
    private String announcement;

    /**
     * 关于页面内容。
     */
    private String aboutContent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
