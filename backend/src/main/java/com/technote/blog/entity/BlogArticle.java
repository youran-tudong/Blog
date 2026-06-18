package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体，对应 blog_article 表。
 */
@Data
@TableName("blog_article")
public class BlogArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题。
     */
    private String title;

    /**
     * 文章访问标识。
     */
    private String slug;

    /**
     * 文章摘要。
     */
    private String summary;

    /**
     * Markdown 正文。
     */
    private String content;

    /**
     * 封面图地址。
     */
    private String coverUrl;

    /**
     * 分类ID。
     */
    private Long categoryId;

    /**
     * 专栏ID。
     */
    private Long columnId;

    /**
     * 是否置顶：0否 1是。
     */
    private Integer topFlag;

    /**
     * 可见性：0私密 1公开。
     */
    private Integer visibility;

    /**
     * 状态：0草稿 1发布 2自动草稿。
     */
    private Integer status;

    /**
     * 阅读量。
     */
    private Long viewCount;

    /**
     * 点赞数。
     */
    private Long likeCount;

    /**
     * 发布时间。
     */
    private LocalDateTime publishTime;

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

