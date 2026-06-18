package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 回收站分页查询请求。
 */
@Data
public class RecyclePageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;

    /**
     * 资源类型：ARTICLE 等。
     */
    private String resourceType;

    /**
     * 标题关键词。
     */
    private String keyword;
}
