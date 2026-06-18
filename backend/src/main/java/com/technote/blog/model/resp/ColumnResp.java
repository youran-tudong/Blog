package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专栏响应。
 */
@Data
public class ColumnResp {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private String coverUrl;

    private Integer sortOrder;

    private Integer status;

    private Long articleCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
