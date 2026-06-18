package com.technote.blog.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 自动草稿过期清理请求。
 */
@Data
public class ArticleAutoDraftCleanupReq {

    /**
     * 保留天数，默认 30 天。
     */
    @Min(value = 1, message = "保留天数必须大于0")
    @Max(value = 3650, message = "保留天数不能超过3650")
    private Integer retentionDays = 30;

    /**
     * 是否只清理未关联正式文章的新文章草稿，默认只清理孤立草稿。
     */
    private Boolean onlyOrphan = true;
}
