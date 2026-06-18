package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链申请响应。
 */
@Data
public class LinkApplyResp {

    private Long id;

    private String siteName;

    private String siteUrl;

    private String iconUrl;

    private String description;

    private String applicantEmail;

    private Integer status;

    private String auditRemark;

    private Long auditBy;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
