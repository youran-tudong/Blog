package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 前台公开文章分页查询请求。
 */
@Data
public class PublicArticlePageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 50, message = "每页数量不能超过50")
    private Long pageSize = 10L;

    /**
     * 标题、摘要或访问标识关键词。
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
}
