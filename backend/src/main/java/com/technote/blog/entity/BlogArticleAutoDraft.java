package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章自动草稿实体，对应 blog_article_auto_draft 表。
 */
@Data
@TableName("blog_article_auto_draft")
public class BlogArticleAutoDraft {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 前端草稿键，同一用户内唯一。
     */
    private String draftKey;

    /**
     * 关联文章ID，新文章草稿为空。
     */
    private Long articleId;

    /**
     * 草稿标题。
     */
    private String title;

    /**
     * 草稿访问标识。
     */
    private String slug;

    /**
     * 草稿摘要。
     */
    private String summary;

    /**
     * Markdown 草稿正文。
     */
    private String content;

    /**
     * 草稿封面。
     */
    private String coverUrl;

    private Long categoryId;

    private Long columnId;

    /**
     * 标签ID JSON数组。
     */
    private String tagIds;

    /**
     * 是否置顶：0否 1是。
     */
    private Integer topFlag;

    /**
     * 可见性：0私密 1公开。
     */
    private Integer visibility;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除 1已删除。
     */
    @TableLogic
    private Integer deleted;
}
