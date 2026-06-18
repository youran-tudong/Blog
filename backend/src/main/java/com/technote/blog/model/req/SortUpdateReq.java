package com.technote.blog.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 排序更新请求。
 */
@Data
public class SortUpdateReq {

    /**
     * 排序值，越小越靠前。
     */
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}

