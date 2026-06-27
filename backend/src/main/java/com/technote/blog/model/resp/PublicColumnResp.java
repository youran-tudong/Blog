package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 前台专栏展示响应。
 */
@Data
public class PublicColumnResp {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private String coverUrl;

    private Long articleCount;
}
