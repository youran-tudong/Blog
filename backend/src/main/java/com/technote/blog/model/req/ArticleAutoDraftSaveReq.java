package com.technote.blog.model.req;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章自动草稿保存请求。
 */
@Data
public class ArticleAutoDraftSaveReq {

    /**
     * 前端草稿键，同一用户内唯一。新文章使用临时键，已有文章建议使用 article-{id}。
     */
    @Size(max = 80, message = "草稿键不能超过80个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "草稿键只能包含字母、数字、下划线和短横线")
    private String draftKey;

    /**
     * 正在编辑的正式文章ID，新文章草稿为空。
     */
    private Long articleId;

    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    @Size(max = 220, message = "文章访问标识不能超过220个字符")
    @Pattern(regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "文章访问标识只能包含小写字母、数字和短横线")
    private String slug;

    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    /**
     * Markdown 正文草稿，允许为空，便于空白新文章自动保存。
     */
    private String content;

    @Size(max = 500, message = "封面图地址不能超过500个字符")
    private String coverUrl;

    private Long categoryId;

    private Long columnId;

    private List<Long> tagIds;

    /**
     * 是否置顶：0否 1是。为空时按 0 保存。
     */
    private Integer topFlag;

    /**
     * 可见性：0私密 1公开。为空时按 1 保存。
     */
    private Integer visibility;
}
