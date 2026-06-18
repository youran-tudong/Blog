package com.technote.blog.model.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章批量分类更新请求。
 */
@Data
public class ArticleBatchCategoryReq {

    /**
     * 文章ID列表。
     */
    @NotEmpty(message = "请选择文章")
    @Size(max = 100, message = "单次最多操作100篇文章")
    private List<Long> articleIds;

    /**
     * 分类ID，为空时表示移出分类。
     */
    private Long categoryId;
}
