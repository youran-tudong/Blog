package com.technote.blog.model.resp;

import lombok.Data;

import java.util.List;

/**
 * 前台文章归档月份响应。
 */
@Data
public class ArchiveMonthResp {

    /**
     * 月份，格式 yyyy-MM。
     */
    private String month;

    /**
     * 月份展示名称。
     */
    private String monthLabel;

    /**
     * 当月公开文章列表。
     */
    private List<ArticleListResp> articles;
}
