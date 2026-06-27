package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 前台分类展示响应。
 */
@Data
public class PublicCategoryResp {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private Long articleCount;
}
