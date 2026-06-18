package com.technote.blog.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 专栏保存请求。
 */
@Data
public class ColumnSaveReq {

    /**
     * 专栏名称。
     */
    @NotBlank(message = "专栏名称不能为空")
    @Size(max = 100, message = "专栏名称不能超过100个字符")
    private String name;

    /**
     * 专栏访问标识。
     */
    @NotBlank(message = "专栏访问标识不能为空")
    @Size(max = 120, message = "专栏访问标识不能超过120个字符")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "专栏访问标识只能包含小写字母、数字和短横线")
    private String slug;

    /**
     * 专栏简介。
     */
    @Size(max = 500, message = "专栏简介不能超过500个字符")
    private String description;

    /**
     * 专栏封面。
     */
    @Size(max = 500, message = "专栏封面不能超过500个字符")
    private String coverUrl;

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
