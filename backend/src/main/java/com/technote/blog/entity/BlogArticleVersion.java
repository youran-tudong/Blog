package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章历史版本实体，对应 blog_article_version 表。
 */
@Data
@TableName("blog_article_version")
public class BlogArticleVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID。
     */
    private Long articleId;

    /**
     * 版本标题。
     */
    private String title;

    /**
     * 版本摘要。
     */
    private String summary;

    /**
     * 版本正文。
     */
    private String content;

    /**
     * 版本号，从1递增。
     */
    private Integer versionNo;

    /**
     * 版本备注。
     */
    private String versionRemark;

    private Long createBy;

    private LocalDateTime createTime;
}

