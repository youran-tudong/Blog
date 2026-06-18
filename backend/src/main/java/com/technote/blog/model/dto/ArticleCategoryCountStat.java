package com.technote.blog.model.dto;

import lombok.Data;

/**
 * 文章分类数量统计。
 */
@Data
public class ArticleCategoryCountStat {

    private Long categoryId;

    private String categoryName;

    private Long articleCount;
}
