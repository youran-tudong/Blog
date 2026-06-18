package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分类/标签分页查询请求。
 */
@Data
public class TaxonomyPageQueryReq {

    /**
     * 当前页码。
     */
    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    /**
     * 每页数量。
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;

    /**
     * 名称或访问标识关键词。
     */
    private String keyword;

    /**
     * 状态：0隐藏 1显示。
     */
    private Integer status;
}

