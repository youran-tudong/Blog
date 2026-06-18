package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 审核列表分页查询请求。
 */
@Data
public class AuditPageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;

    /**
     * 状态：0待审核 1通过 2驳回。
     */
    private Integer status;

    /**
     * 文章ID，仅评论列表使用。
     */
    private Long articleId;
}
