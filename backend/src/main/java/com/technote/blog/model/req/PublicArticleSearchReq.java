package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台公开文章全文搜索请求。
 */
@Data
public class PublicArticleSearchReq {

    @Min(value = 1, message = "页码必须大于0")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 50, message = "每页数量不能超过50")
    private Long pageSize = 10L;

    /**
     * 搜索关键词，匹配标题、摘要、正文和访问标识。
     */
    @NotBlank(message = "搜索关键词不能为空")
    @Size(max = 100, message = "搜索关键词不能超过100个字符")
    private String keyword;

    /**
     * 排序方式：latest 最新，views 阅读量。
     */
    private String sort = "latest";
}
