package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 文章 Markdown 导出文件。
 */
@Data
public class ArticleExportFileResp {

    /**
     * 下载文件名。
     */
    private String fileName;

    /**
     * Markdown 文件内容。
     */
    private String content;
}
