package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 前台友情链接展示响应。
 */
@Data
public class PublicLinkResp {

    private Long id;

    private String siteName;

    private String siteUrl;

    private String iconUrl;

    private String description;
}
