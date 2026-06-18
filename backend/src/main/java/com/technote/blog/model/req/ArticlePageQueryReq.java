package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 后台文章分页查询请求。
 */
@Data
public class ArticlePageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;

    /**
     * 标题或访问标识关键词。
     */
    private String keyword;

    /**
     * 分类ID。
     */
    private Long categoryId;

    /**
     * 标签ID。
     */
    private Long tagId;

    /**
     * 专栏ID。
     */
    private Long columnId;

    /**
     * 状态：0草稿 1发布 2自动草稿。
     */
    private Integer status;

    /**
     * 可见性：0私密 1公开。
     */
    private Integer visibility;
}
