package com.technote.log.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 操作日志分页查询请求。
 */
@Data
public class OperationLogPageQueryReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Long pageSize = 10L;

    /**
     * 模块关键词。
     */
    private String module;

    /**
     * 成功状态：0失败 1成功。
     */
    private Integer successFlag;
}
