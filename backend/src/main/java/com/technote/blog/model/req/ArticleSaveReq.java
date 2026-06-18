package com.technote.blog.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章保存请求。
 */
@Data
public class ArticleSaveReq {

    /**
     * 文章标题。
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    /**
     * 文章访问标识。
     */
    @NotBlank(message = "文章访问标识不能为空")
    @Size(max = 220, message = "文章访问标识不能超过220个字符")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "文章访问标识只能包含小写字母、数字和短横线")
    private String slug;

    /**
     * 文章摘要。
     */
    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    /**
     * Markdown正文。
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 封面图地址。
     */
    @Size(max = 500, message = "封面图地址不能超过500个字符")
    private String coverUrl;

    /**
     * 分类ID。
     */
    private Long categoryId;

    /**
     * 专栏ID。
     */
    private Long columnId;

    /**
     * 标签ID列表。
     */
    private List<Long> tagIds;

    /**
     * 是否置顶：0否 1是。
     */
    @NotNull(message = "置顶状态不能为空")
    private Integer topFlag;

    /**
     * 可见性：0私密 1公开。
     */
    @NotNull(message = "可见性不能为空")
    private Integer visibility;

    /**
     * 状态：0草稿 1发布 2自动草稿。
     */
    @NotNull(message = "文章状态不能为空")
    private Integer status;
}
