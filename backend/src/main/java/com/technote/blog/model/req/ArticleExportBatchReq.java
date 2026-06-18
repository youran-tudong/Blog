package com.technote.blog.model.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章批量导出请求。
 */
@Data
public class ArticleExportBatchReq {

    /**
     * 待导出的文章ID列表。
     */
    @NotEmpty(message = "请选择要导出的文章")
    @Size(max = 100, message = "单次最多导出100篇文章")
    private List<Long> articleIds;
}
