package com.technote.blog.service;

import com.technote.blog.model.resp.ArticleExportFileResp;

import java.util.List;

/**
 * 文章导出服务。
 */
public interface ArticleExportService {

    /**
     * 导出单篇文章为 Markdown。
     *
     * @param id 文章ID
     * @return Markdown 导出文件
     */
    ArticleExportFileResp exportArticle(Long id);

    /**
     * 批量导出文章为一个 Markdown 文件。
     *
     * @param articleIds 文章ID列表
     * @return Markdown 导出文件
     */
    ArticleExportFileResp exportArticles(List<Long> articleIds);
}
