package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 友链申请审核请求。
 */
@Data
public class LinkApplyAuditReq {

    /**
     * 状态：1通过 2驳回。
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态不正确")
    @Max(value = 2, message = "审核状态不正确")
    private Integer status;

    /**
     * 审核备注，驳回时必填。
     */
    @Size(max = 500, message = "审核备注不能超过500个字符")
    private String auditRemark;
}
