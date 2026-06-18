package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专栏文章关联响应。
 */
@Data
public class ColumnArticleResp {

    private Long articleId;

    private String title;

    private String slug;

    private Integer status;

    private Integer visibility;

    private Integer sortOrder;

    private LocalDateTime publishTime;

    private LocalDateTime updateTime;
}
