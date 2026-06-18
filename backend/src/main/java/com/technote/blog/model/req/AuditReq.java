package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核请求。
 */
@Data
public class AuditReq {

    /**
     * 状态：1通过 2驳回。
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态不正确")
    @Max(value = 2, message = "审核状态不正确")
    private Integer status;

    /**
     * 管理员回复。
     */
    @Size(max = 1000, message = "回复内容不能超过1000个字符")
    private String replyContent;
}
