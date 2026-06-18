package com.technote.blog.model.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章详情响应，包含完整 Markdown 正文。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleResp extends ArticleListResp {

    /**
     * Markdown 正文，仅详情、编辑、保存和回滚等接口返回。
     */
    private String content;
}
