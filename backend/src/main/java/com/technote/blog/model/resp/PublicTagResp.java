package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 前台标签展示响应。
 */
@Data
public class PublicTagResp {

    private Long id;

    private String name;

    private String slug;

    private String color;

    private Long articleCount;
}
