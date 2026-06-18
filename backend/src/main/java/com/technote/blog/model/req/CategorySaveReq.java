package com.technote.blog.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类保存请求。
 */
@Data
public class CategorySaveReq {

    /**
     * 分类名称。
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称不能超过64个字符")
    private String name;

    /**
     * 分类访问标识。
     */
    @NotBlank(message = "分类访问标识不能为空")
    @Size(max = 80, message = "分类访问标识不能超过80个字符")
    private String slug;

    /**
     * 分类描述。
     */
    @Size(max = 255, message = "分类描述不能超过255个字符")
    private String description;

    /**
     * 排序值，越小越靠前。
     */
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;

    /**
     * 状态：0隐藏 1显示。
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

