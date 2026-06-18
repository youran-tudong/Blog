package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专栏文章关联实体，对应 blog_column_article 表。
 */
@Data
@TableName("blog_column_article")
public class BlogColumnArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 专栏ID。
     */
    private Long columnId;

    /**
     * 文章ID。
     */
    private Long articleId;

    /**
     * 专栏内排序值。
     */
    private Integer sortOrder;

    private LocalDateTime createTime;
}
