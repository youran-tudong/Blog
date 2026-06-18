package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章自动草稿响应。
 */
@Data
public class ArticleAutoDraftResp {

    private Long id;

    private String draftKey;

    private Long articleId;

    private String title;

    private String slug;

    private String summary;

    private String content;

    private String coverUrl;

    private Long categoryId;

    private Long columnId;

    private List<Long> tagIds;

    private Integer topFlag;

    private Integer visibility;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
