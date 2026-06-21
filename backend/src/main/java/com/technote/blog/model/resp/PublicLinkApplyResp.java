package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前台公开友链申请响应，不包含申请邮箱和后台审核信息。
 */
@Data
public class PublicLinkApplyResp {

    private Long id;

    private String siteName;

    private String siteUrl;

    private String iconUrl;

    private String description;

    private LocalDateTime createTime;
}
