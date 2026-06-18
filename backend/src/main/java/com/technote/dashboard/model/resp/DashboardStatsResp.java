package com.technote.dashboard.model.resp;

import lombok.Data;

import java.util.List;

/**
 * 后台仪表盘统计响应。
 */
@Data
public class DashboardStatsResp {

    private Long articleTotal;

    private Long publishedArticleCount;

    private Long draftArticleCount;

    private Long autoDraftCount;

    private Long viewTotal;

    private Long categoryCount;

    private Long tagCount;

    private Long columnCount;

    private Long mediaCount;

    private Long pendingCommentCount;

    private Long pendingGuestbookCount;

    private Long operationLogCount;

    private List<DashboardRecentArticleResp> recentArticles;

    private List<DashboardPopularArticleResp> popularArticles;

    private List<DashboardTrendDayResp> trendDays;

    private List<DashboardCategoryDistributionResp> categoryDistribution;

    private List<DashboardRecentOperationResp> recentOperations;
}
