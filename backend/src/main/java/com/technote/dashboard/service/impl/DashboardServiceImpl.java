package com.technote.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogArticleAutoDraft;
import com.technote.blog.entity.BlogCategory;
import com.technote.blog.entity.BlogColumn;
import com.technote.blog.entity.BlogComment;
import com.technote.blog.entity.BlogGuestbook;
import com.technote.blog.entity.BlogMedia;
import com.technote.blog.entity.BlogTag;
import com.technote.blog.enums.ArticleStatusEnum;
import com.technote.blog.enums.ArticleVisibilityEnum;
import com.technote.blog.enums.AuditStatusEnum;
import com.technote.blog.mapper.BlogArticleAutoDraftMapper;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogCategoryMapper;
import com.technote.blog.mapper.BlogArticleViewDailyMapper;
import com.technote.blog.mapper.BlogColumnMapper;
import com.technote.blog.mapper.BlogCommentMapper;
import com.technote.blog.mapper.BlogGuestbookMapper;
import com.technote.blog.mapper.BlogMediaMapper;
import com.technote.blog.mapper.BlogTagMapper;
import com.technote.dashboard.model.resp.DashboardRecentArticleResp;
import com.technote.dashboard.model.resp.DashboardRecentOperationResp;
import com.technote.dashboard.model.resp.DashboardPopularArticleResp;
import com.technote.dashboard.model.resp.DashboardCategoryDistributionResp;
import com.technote.dashboard.model.resp.DashboardStatsResp;
import com.technote.dashboard.model.resp.DashboardTrendDayResp;
import com.technote.dashboard.service.DashboardService;
import com.technote.log.entity.OperationLogRecord;
import com.technote.log.mapper.OperationLogRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 后台仪表盘统计服务实现。
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_LIMIT = 5;

    private static final int TREND_DAYS = 30;

    private final BlogArticleMapper articleMapper;

    private final BlogArticleViewDailyMapper articleViewDailyMapper;

    private final BlogArticleAutoDraftMapper articleAutoDraftMapper;

    private final BlogCategoryMapper categoryMapper;

    private final BlogTagMapper tagMapper;

    private final BlogColumnMapper columnMapper;

    private final BlogMediaMapper mediaMapper;

    private final BlogCommentMapper commentMapper;

    private final BlogGuestbookMapper guestbookMapper;

    private final OperationLogRecordMapper operationLogRecordMapper;

    @Override
    public DashboardStatsResp getStats() {
        DashboardStatsResp resp = new DashboardStatsResp();
        resp.setArticleTotal(articleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()));
        resp.setPublishedArticleCount(countArticleByStatus(ArticleStatusEnum.PUBLISHED.getCode()));
        resp.setDraftArticleCount(countArticleByStatus(ArticleStatusEnum.DRAFT.getCode()));
        resp.setAutoDraftCount(articleAutoDraftMapper.selectCount(new LambdaQueryWrapper<BlogArticleAutoDraft>()));
        resp.setViewTotal(articleMapper.selectTotalViewCount());
        resp.setCategoryCount(categoryMapper.selectCount(new LambdaQueryWrapper<BlogCategory>()));
        resp.setTagCount(tagMapper.selectCount(new LambdaQueryWrapper<BlogTag>()));
        resp.setColumnCount(columnMapper.selectCount(new LambdaQueryWrapper<BlogColumn>()));
        resp.setMediaCount(mediaMapper.selectCount(new LambdaQueryWrapper<BlogMedia>()));
        resp.setPendingCommentCount(commentMapper.selectCount(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getStatus, AuditStatusEnum.PENDING.getCode())));
        resp.setPendingGuestbookCount(guestbookMapper.selectCount(new LambdaQueryWrapper<BlogGuestbook>()
                .eq(BlogGuestbook::getStatus, AuditStatusEnum.PENDING.getCode())));
        resp.setOperationLogCount(operationLogRecordMapper.selectCount(new LambdaQueryWrapper<OperationLogRecord>()));
        resp.setRecentArticles(listRecentArticles());
        resp.setPopularArticles(listPopularArticles());
        resp.setTrendDays(listTrendDays());
        resp.setCategoryDistribution(listCategoryDistribution());
        resp.setRecentOperations(listRecentOperations());
        return resp;
    }

    private Long countArticleByStatus(Integer status) {
        return articleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, status));
    }

    private List<DashboardRecentArticleResp> listRecentArticles() {
        return articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                        .orderByDesc(BlogArticle::getUpdateTime)
                        .last("LIMIT " + RECENT_LIMIT))
                .stream()
                .map(this::toRecentArticleResp)
                .toList();
    }

    private List<DashboardRecentOperationResp> listRecentOperations() {
        return operationLogRecordMapper.selectList(new LambdaQueryWrapper<OperationLogRecord>()
                        .orderByDesc(OperationLogRecord::getCreateTime)
                        .last("LIMIT " + RECENT_LIMIT))
                .stream()
                .map(this::toRecentOperationResp)
                .toList();
    }

    private List<DashboardPopularArticleResp> listPopularArticles() {
        return articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                        .select(
                                BlogArticle::getId,
                                BlogArticle::getTitle,
                                BlogArticle::getSlug,
                                BlogArticle::getViewCount,
                                BlogArticle::getPublishTime)
                        .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                        .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                        .orderByDesc(BlogArticle::getViewCount)
                        .orderByDesc(BlogArticle::getPublishTime)
                        .last("LIMIT " + RECENT_LIMIT))
                .stream()
                .map(this::toPopularArticleResp)
                .toList();
    }

    private List<DashboardTrendDayResp> listTrendDays() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(TREND_DAYS - 1L);
        Map<LocalDate, Long> viewCountMap = articleViewDailyMapper.selectDailyViewCounts(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(item -> item.getStatDate(), item -> item.getCount()));
        Map<LocalDate, Long> publishCountMap = articleMapper.selectDailyPublishCounts(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(item -> item.getStatDate(), item -> item.getCount()));
        return IntStream.range(0, TREND_DAYS)
                .mapToObj(index -> {
                    LocalDate date = startDate.plusDays(index);
                    DashboardTrendDayResp resp = new DashboardTrendDayResp();
                    resp.setDate(date);
                    resp.setViewCount(viewCountMap.getOrDefault(date, 0L));
                    resp.setPublishCount(publishCountMap.getOrDefault(date, 0L));
                    return resp;
                })
                .toList();
    }

    private List<DashboardCategoryDistributionResp> listCategoryDistribution() {
        return articleMapper.selectCategoryArticleCounts()
                .stream()
                .map(item -> {
                    DashboardCategoryDistributionResp resp = new DashboardCategoryDistributionResp();
                    resp.setCategoryId(item.getCategoryId());
                    resp.setCategoryName(item.getCategoryName());
                    resp.setArticleCount(item.getArticleCount());
                    return resp;
                })
                .toList();
    }

    private DashboardRecentArticleResp toRecentArticleResp(BlogArticle article) {
        DashboardRecentArticleResp resp = new DashboardRecentArticleResp();
        resp.setId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSlug(article.getSlug());
        resp.setStatus(article.getStatus());
        resp.setVisibility(article.getVisibility());
        resp.setViewCount(article.getViewCount());
        resp.setUpdateTime(article.getUpdateTime());
        return resp;
    }

    private DashboardRecentOperationResp toRecentOperationResp(OperationLogRecord record) {
        DashboardRecentOperationResp resp = new DashboardRecentOperationResp();
        resp.setId(record.getId());
        resp.setUserId(record.getUserId());
        resp.setModule(record.getModule());
        resp.setOperation(record.getOperation());
        resp.setSuccessFlag(record.getSuccessFlag());
        resp.setCreateTime(record.getCreateTime());
        return resp;
    }

    private DashboardPopularArticleResp toPopularArticleResp(BlogArticle article) {
        DashboardPopularArticleResp resp = new DashboardPopularArticleResp();
        resp.setId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSlug(article.getSlug());
        resp.setViewCount(article.getViewCount());
        resp.setPublishTime(article.getPublishTime());
        return resp;
    }
}
