package com.technote.blog.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 专栏文章关联保存请求。
 */
@Data
public class ColumnArticleSaveReq {

    /**
     * 文章ID。
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * 专栏内排序值。为空时自动追加到末尾。
     */
    private Integer sortOrder;
}
