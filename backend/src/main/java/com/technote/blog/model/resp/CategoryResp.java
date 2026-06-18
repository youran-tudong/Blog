package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类响应。
 */
@Data
public class CategoryResp {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private Long articleCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

