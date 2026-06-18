package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表响应，不包含完整正文，避免列表接口传输过大的 Markdown 内容。
 */
@Data
public class ArticleListResp {

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String coverUrl;

    private Long categoryId;

    private String categoryName;

    private Long columnId;

    private String columnName;

    private String columnSlug;

    private List<Long> tagIds;

    private List<String> tagNames;

    private Integer topFlag;

    private Integer visibility;

    private Integer status;

    private Long viewCount;

    private Long likeCount;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
