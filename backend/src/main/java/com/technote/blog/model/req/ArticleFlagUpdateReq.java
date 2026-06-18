package com.technote.blog.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文章开关状态更新请求。
 */
@Data
public class ArticleFlagUpdateReq {

    /**
     * 开关值：0否 1是，或对应业务状态。
     */
    @NotNull(message = "状态值不能为空")
    private Integer value;
}

