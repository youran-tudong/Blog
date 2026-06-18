package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 前台文章搜索结果响应。
 */
@Data
public class ArticleSearchResp {

    private Long id;

    private String title;

    private String slug;

    private String summary;

    /**
     * 命中的正文或摘要片段，纯文本，由前端安全高亮。
     */
    private String snippet;

    private String categoryName;

    private String columnName;

    private List<String> tagNames;

    private Long viewCount;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;
}
