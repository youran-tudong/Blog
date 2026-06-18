package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签响应。
 */
@Data
public class TagResp {

    private Long id;

    private String name;

    private String slug;

    private String color;

    private Long articleCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

