package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台自动草稿列表响应。
 */
@Data
public class ArticleAutoDraftListResp {

    private Long id;

    /**
     * 前端草稿键。
     */
    private String draftKey;

    /**
     * 关联文章ID，为空表示新文章草稿。
     */
    private Long articleId;

    private String title;

    private String slug;

    private String summary;

    /**
     * 正文预览片段，列表页不返回完整正文。
     */
    private String contentPreview;

    /**
     * 正文字符数，便于判断草稿体积。
     */
    private Integer contentLength;

    private Long categoryId;

    private Long columnId;

    private Integer topFlag;

    private Integer visibility;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;
}
