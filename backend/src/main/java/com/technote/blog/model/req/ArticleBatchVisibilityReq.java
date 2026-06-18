package com.technote.blog.model.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章批量可见性更新请求。
 */
@Data
public class ArticleBatchVisibilityReq {

    /**
     * 文章ID列表。
     */
    @NotEmpty(message = "请选择文章")
    @Size(max = 100, message = "单次最多操作100篇文章")
    private List<Long> articleIds;

    /**
     * 可见性：0私密 1公开。
     */
    @NotNull(message = "可见性不能为空")
    private Integer visibility;
}
