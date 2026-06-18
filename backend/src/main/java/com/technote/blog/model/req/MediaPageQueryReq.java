package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 媒体文件分页查询请求。
 */
@Data
public class MediaPageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 12L;

    /**
     * 原始文件名关键词。
     */
    private String keyword;
}
