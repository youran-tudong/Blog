package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 后台自动草稿分页查询请求。
 */
@Data
public class ArticleAutoDraftPageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;

    /**
     * 草稿标题、访问标识、草稿键关键字。
     */
    private String keyword;

    /**
     * 关联文章ID。
     */
    private Long articleId;

    /**
     * 草稿创建人。
     */
    private Long createBy;

    /**
     * 是否只查询未关联正式文章的新文章草稿。
     */
    private Boolean onlyOrphan;
}
