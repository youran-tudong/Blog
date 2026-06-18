package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友情链接响应。
 */
@Data
public class LinkResp {

    private Long id;

    private String siteName;

    private String siteUrl;

    private String iconUrl;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
