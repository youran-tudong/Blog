package com.technote.blog.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 标签保存请求。
 */
@Data
public class TagSaveReq {

    /**
     * 标签名称。
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 64, message = "标签名称不能超过64个字符")
    private String name;

    /**
     * 标签访问标识。
     */
    @NotBlank(message = "标签访问标识不能为空")
    @Size(max = 80, message = "标签访问标识不能超过80个字符")
    private String slug;

    /**
     * 标签颜色。
     */
    @Size(max = 32, message = "标签颜色不能超过32个字符")
    private String color;
}

