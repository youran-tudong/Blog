package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章标签关联实体，对应 blog_article_tag 表。
 */
@Data
@TableName("blog_article_tag")
public class BlogArticleTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID。
     */
    private Long articleId;

    /**
     * 标签ID。
     */
    private Long tagId;

    private LocalDateTime createTime;
}

