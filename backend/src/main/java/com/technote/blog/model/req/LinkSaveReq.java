package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 友情链接保存请求。
 */
@Data
public class LinkSaveReq {

    /**
     * 网站名称。
     */
    @NotBlank(message = "网站名称不能为空")
    @Size(max = 100, message = "网站名称不能超过100个字符")
    private String siteName;

    /**
     * 网站地址。
     */
    @NotBlank(message = "网站地址不能为空")
    @Size(max = 255, message = "网站地址不能超过255个字符")
    private String siteUrl;

    /**
     * 网站图标。
     */
    @Size(max = 500, message = "网站图标不能超过500个字符")
    private String iconUrl;

    /**
     * 网站描述。
     */
    @Size(max = 255, message = "网站描述不能超过255个字符")
    private String description;

    /**
     * 排序值。
     */
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;

    /**
     * 状态：0隐藏 1显示。
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status;
}
