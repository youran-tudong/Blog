package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章匿名点赞关系实体，对应 blog_article_like 表。
 */
@Data
@TableName("blog_article_like")
public class BlogArticleLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID。
     */
    private Long articleId;

    /**
     * 浏览器本地生成的匿名访客标识。
     */
    private String visitorKey;

    private LocalDateTime createTime;
}

