package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文章每日阅读量聚合实体，对应 blog_article_view_daily 表。
 */
@Data
@TableName("blog_article_view_daily")
public class BlogArticleViewDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID。
     */
    private Long articleId;

    /**
     * 统计日期。
     */
    private LocalDate statDate;

    /**
     * 当日阅读量。
     */
    private Long viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
