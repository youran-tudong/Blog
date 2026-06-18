package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章历史版本列表响应。
 */
@Data
public class ArticleVersionResp {

    private Long id;

    private Long articleId;

    private String title;

    private String summary;

    private Integer versionNo;

    private String versionRemark;

    private Long createBy;

    private LocalDateTime createTime;
}
