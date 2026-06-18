package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogArticleTag;
import com.technote.blog.entity.BlogArticleVersion;
import com.technote.blog.entity.BlogCategory;
import com.technote.blog.entity.BlogColumn;
import com.technote.blog.entity.BlogColumnArticle;
import com.technote.blog.entity.BlogTag;
import com.technote.blog.enums.ArticleStatusEnum;
import com.technote.blog.enums.ArticleVisibilityEnum;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogArticleTagMapper;
import com.technote.blog.mapper.BlogArticleViewDailyMapper;
import com.technote.blog.mapper.BlogArticleVersionMapper;
import com.technote.blog.mapper.BlogCategoryMapper;
import com.technote.blog.mapper.BlogColumnArticleMapper;
import com.technote.blog.mapper.BlogColumnMapper;
import com.technote.blog.mapper.BlogTagMapper;
import com.technote.blog.model.dto.ArticleDailyCountStat;
import com.technote.blog.model.req.ArticleFlagUpdateReq;
import com.technote.blog.model.req.ArticlePageQueryReq;
import com.technote.blog.model.req.ArticleSaveReq;
import com.technote.blog.model.req.PublicArticlePageQueryReq;
import com.technote.blog.model.req.PublicArticleSearchReq;
import com.technote.blog.model.resp.ArchiveHeatmapDayResp;
import com.technote.blog.model.resp.ArchiveHeatmapResp;
import com.technote.blog.model.resp.ArchiveMonthResp;
import com.technote.blog.model.resp.ArticleListResp;
import com.technote.blog.model.resp.ArticleResp;
import com.technote.blog.model.resp.ArticleSearchResp;
import com.technote.blog.model.resp.ArticleVersionDetailResp;
import com.technote.blog.model.resp.ArticleVersionResp;
import com.technote.blog.service.ArticleService;
import com.technote.blog.service.MediaService;
import com.technote.blog.service.RecycleService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final BlogArticleMapper articleMapper;

    private final BlogArticleViewDailyMapper articleViewDailyMapper;

    private final BlogArticleTagMapper articleTagMapper;

    private final BlogArticleVersionMapper articleVersionMapper;

    private final BlogCategoryMapper categoryMapper;

    private final BlogColumnMapper columnMapper;

    private final BlogColumnArticleMapper columnArticleMapper;

    private final BlogTagMapper tagMapper;

    private final MediaService mediaService;

    private final RecycleService recycleService;

    @Override
    public PageResp<ArticleListResp> pageArticles(ArticlePageQueryReq req) {
        IPage<BlogArticle> page = articleMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildArticleQuery(req));
        List<ArticleListResp> records = toArticleListRespList(page.getRecords());
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ArticleResp getArticle(Long id) {
        return toArticleResp(getArticleOrThrow(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResp createArticle(ArticleSaveReq req) {
        validateArticleReq(req);
        ensureSlugUnique(null, req.getSlug());
        BlogArticle article = new BlogArticle();
        fillArticle(article, req);
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setCreateBy(currentUserId());
        article.setUpdateBy(currentUserId());
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(article.getStatus())) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.insert(article);
        saveArticleTags(article.getId(), req.getTagIds());
        syncArticleColumnRelation(article.getId(), null, article.getColumnId());
        mediaService.refreshArticleMediaQuotes(null, null, article.getCoverUrl(), article.getContent());
        return toArticleResp(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResp updateArticle(Long id, ArticleSaveReq req) {
        validateArticleReq(req);
        BlogArticle article = getArticleOrThrow(id);
        ensureSlugUnique(id, req.getSlug());
        String oldCoverUrl = article.getCoverUrl();
        String oldContent = article.getContent();
        Long oldColumnId = article.getColumnId();
        createVersionSnapshot(article, "编辑前自动保存");
        fillArticle(article, req);
        article.setUpdateBy(currentUserId());
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(article.getStatus()) && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.updateById(article);
        saveArticleTags(article.getId(), req.getTagIds());
        syncArticleColumnRelation(article.getId(), oldColumnId, article.getColumnId());
        mediaService.refreshArticleMediaQuotes(oldCoverUrl, oldContent, article.getCoverUrl(), article.getContent());
        return toArticleResp(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        deleteArticleInternal(getArticleOrThrow(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteArticles(List<Long> articleIds) {
        for (Long articleId : normalizeBatchArticleIds(articleIds)) {
            deleteArticleInternal(getArticleOrThrow(articleId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateVisibility(List<Long> articleIds, Integer visibility) {
        if (!ArticleVisibilityEnum.contains(visibility)) {
            throw new BaseException(400, "可见性状态不正确");
        }
        Long userId = currentUserId();
        for (Long articleId : normalizeBatchArticleIds(articleIds)) {
            BlogArticle article = getArticleOrThrow(articleId);
            article.setVisibility(visibility);
            article.setUpdateBy(userId);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCategory(List<Long> articleIds, Long categoryId) {
        if (categoryId != null && categoryMapper.selectById(categoryId) == null) {
            throw new BaseException(400, "分类不存在");
        }
        Long userId = currentUserId();
        for (Long articleId : normalizeBatchArticleIds(articleIds)) {
            BlogArticle article = getArticleOrThrow(articleId);
            article.setCategoryId(categoryId);
            article.setUpdateBy(userId);
            articleMapper.updateById(article);
        }
    }

    @Override
    public List<ArticleVersionResp> listArticleVersions(Long id) {
        getArticleOrThrow(id);
        return articleVersionMapper.selectList(new LambdaQueryWrapper<BlogArticleVersion>()
                        .eq(BlogArticleVersion::getArticleId, id)
                        .orderByDesc(BlogArticleVersion::getVersionNo))
                .stream()
                .map(this::toArticleVersionResp)
                .toList();
    }

    @Override
    public ArticleVersionDetailResp getArticleVersion(Long id, Long versionId) {
        getArticleOrThrow(id);
        return toArticleVersionDetailResp(getArticleVersionOrThrow(id, versionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResp rollbackArticleVersion(Long id, Long versionId) {
        BlogArticle article = getArticleOrThrow(id);
        BlogArticleVersion version = getArticleVersionOrThrow(id, versionId);
        String oldCoverUrl = article.getCoverUrl();
        String oldContent = article.getContent();
        createVersionSnapshot(article, "版本回退前自动保存");
        article.setTitle(version.getTitle());
        article.setSummary(version.getSummary());
        article.setContent(version.getContent());
        article.setUpdateBy(currentUserId());
        articleMapper.updateById(article);
        mediaService.refreshArticleMediaQuotes(oldCoverUrl, oldContent, article.getCoverUrl(), article.getContent());
        return toArticleResp(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTopFlag(Long id, ArticleFlagUpdateReq req) {
        validateBinaryFlag(req.getValue(), "置顶状态");
        BlogArticle article = getArticleOrThrow(id);
        article.setTopFlag(req.getValue());
        article.setUpdateBy(currentUserId());
        articleMapper.updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVisibility(Long id, ArticleFlagUpdateReq req) {
        if (!ArticleVisibilityEnum.contains(req.getValue())) {
            throw new BaseException(400, "可见性状态不正确");
        }
        BlogArticle article = getArticleOrThrow(id);
        article.setVisibility(req.getValue());
        article.setUpdateBy(currentUserId());
        articleMapper.updateById(article);
    }

    @Override
    public PageResp<ArticleListResp> pagePublicArticles(PublicArticlePageQueryReq req) {
        IPage<BlogArticle> page = articleMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildPublicArticleQuery(req));
        List<ArticleListResp> records = toArticleListRespList(page.getRecords());
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageResp<ArticleSearchResp> searchPublicArticles(PublicArticleSearchReq req) {
        String keyword = req.getKeyword().trim();
        IPage<BlogArticle> page = articleMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildPublicArticleSearchQuery(keyword, req.getSort()));
        ArticleRenderContext context = buildArticleRenderContext(page.getRecords());
        List<ArticleSearchResp> records = page.getRecords().stream()
                .map(article -> toArticleSearchResp(article, keyword, context))
                .toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResp getPublicArticleBySlug(String slug) {
        BlogArticle article = getPublicArticleBySlugOrThrow(slug);
        // 阅读量只允许公开已发布文章递增，条件更新避免草稿或私密文章被误计数。
        int updated = articleMapper.increasePublicViewCount(article.getId());
        if (updated > 0) {
            articleViewDailyMapper.increaseDailyViewCount(article.getId(), LocalDate.now());
            article.setViewCount((article.getViewCount() == null ? 0L : article.getViewCount()) + 1);
        }
        return toArticleResp(article);
    }

    @Override
    public List<ArticleListResp> listPopularPublicArticles(Integer limit) {
        int size = validatePublicListLimit(limit, 5);
        List<BlogArticle> articles = articleMapper.selectList(selectArticleListFields(new LambdaQueryWrapper<BlogArticle>())
                        .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                        .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                        .orderByDesc(BlogArticle::getViewCount)
                        .orderByDesc(BlogArticle::getPublishTime)
                        .orderByDesc(BlogArticle::getUpdateTime)
                        .last("LIMIT " + size));
        return toArticleListRespList(articles);
    }

    @Override
    public List<ArticleListResp> listRelatedPublicArticles(String slug, Integer limit) {
        int size = validatePublicListLimit(limit, 4);
        BlogArticle current = getPublicArticleBySlugOrThrow(slug);
        Set<Long> currentTagIds = listArticleTagIds(current.getId());
        Set<Long> taggedArticleIds = listArticleIdsByTagIds(currentTagIds);

        LambdaQueryWrapper<BlogArticle> wrapper = selectArticleListFields(new LambdaQueryWrapper<BlogArticle>())
                .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                .ne(BlogArticle::getId, current.getId());
        if (current.getColumnId() != null || current.getCategoryId() != null || CollUtil.isNotEmpty(taggedArticleIds)) {
            wrapper.and(item -> {
                boolean hasCondition = false;
                if (current.getColumnId() != null) {
                    item.eq(BlogArticle::getColumnId, current.getColumnId());
                    hasCondition = true;
                }
                if (current.getCategoryId() != null) {
                    if (hasCondition) {
                        item.or();
                    }
                    item.eq(BlogArticle::getCategoryId, current.getCategoryId());
                    hasCondition = true;
                }
                if (CollUtil.isNotEmpty(taggedArticleIds)) {
                    if (hasCondition) {
                        item.or();
                    }
                    item.in(BlogArticle::getId, taggedArticleIds);
                }
            });
        } else {
            return Collections.emptyList();
        }
        wrapper.orderByDesc(BlogArticle::getViewCount)
                .orderByDesc(BlogArticle::getPublishTime)
                .last("LIMIT 50");

        List<BlogArticle> candidates = articleMapper.selectList(wrapper);
        if (CollUtil.isEmpty(candidates)) {
            return Collections.emptyList();
        }
        Map<Long, Set<Long>> candidateTagIds = listArticleTagIds(candidates.stream().map(BlogArticle::getId).toList());
        List<BlogArticle> relatedArticles = candidates.stream()
                .sorted((left, right) -> compareRelatedArticle(current, currentTagIds, candidateTagIds, left, right))
                .limit(size)
                .toList();
        return toArticleListRespList(relatedArticles);
    }

    @Override
    public List<ArchiveMonthResp> listPublicArchives() {
        List<BlogArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                .orderByDesc(BlogArticle::getPublishTime)
                .orderByDesc(BlogArticle::getUpdateTime));
        ArticleRenderContext context = buildArticleRenderContext(articles);
        Map<YearMonth, List<ArticleListResp>> archiveMap = articles.stream()
                .collect(Collectors.groupingBy(
                        item -> YearMonth.from(resolveArchiveTime(item)),
                        LinkedHashMap::new,
                        Collectors.mapping(item -> toArticleListResp(item, context), Collectors.toList())));
        return archiveMap.entrySet().stream()
                .map(entry -> {
                    ArchiveMonthResp resp = new ArchiveMonthResp();
                    resp.setMonth(entry.getKey().toString());
                    resp.setMonthLabel(entry.getKey().getYear() + " 年 " + entry.getKey().getMonthValue() + " 月");
                    resp.setArticles(entry.getValue());
                    return resp;
                })
                .toList();
    }

    @Override
    public ArchiveHeatmapResp getPublicArchiveHeatmap(Integer year) {
        int targetYear = year == null ? Year.now().getValue() : year;
        if (targetYear < 1970 || targetYear > 2100) {
            throw new BaseException(400, "年份范围不正确");
        }
        LocalDateTime startTime = LocalDate.of(targetYear, 1, 1).atStartOfDay();
        LocalDateTime endTime = startTime.plusYears(1);
        List<ArticleDailyCountStat> stats = articleMapper.selectPublicDailyPublishCounts(startTime, endTime);
        List<ArchiveHeatmapDayResp> days = stats.stream()
                .map(stat -> {
                    ArchiveHeatmapDayResp day = new ArchiveHeatmapDayResp();
                    day.setDate(stat.getStatDate().toString());
                    day.setCount(stat.getCount());
                    return day;
                })
                .toList();
        ArchiveHeatmapResp resp = new ArchiveHeatmapResp();
        resp.setYear(targetYear);
        resp.setTotal(days.stream().mapToLong(ArchiveHeatmapDayResp::getCount).sum());
        resp.setDays(days);
        return resp;
    }

    private LambdaQueryWrapper<BlogArticle> buildArticleQuery(ArticlePageQueryReq req) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getCategoryId() != null, BlogArticle::getCategoryId, req.getCategoryId());
        wrapper.eq(req.getColumnId() != null, BlogArticle::getColumnId, req.getColumnId());
        wrapper.eq(req.getStatus() != null, BlogArticle::getStatus, req.getStatus());
        wrapper.eq(req.getVisibility() != null, BlogArticle::getVisibility, req.getVisibility());
        wrapper.and(StrUtil.isNotBlank(req.getKeyword()), item -> item
                .like(BlogArticle::getTitle, req.getKeyword())
                .or()
                .like(BlogArticle::getSlug, req.getKeyword()));
        if (req.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                            .eq(BlogArticleTag::getTagId, req.getTagId()))
                    .stream()
                    .map(BlogArticleTag::getArticleId)
                    .toList();
            wrapper.in(CollUtil.isNotEmpty(articleIds), BlogArticle::getId, articleIds);
            wrapper.eq(CollUtil.isEmpty(articleIds), BlogArticle::getId, -1L);
        }
        wrapper.orderByDesc(BlogArticle::getTopFlag)
                .orderByDesc(BlogArticle::getPublishTime)
                .orderByDesc(BlogArticle::getUpdateTime);
        return wrapper;
    }

    private LambdaQueryWrapper<BlogArticle> buildPublicArticleQuery(PublicArticlePageQueryReq req) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode());
        wrapper.eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode());
        wrapper.eq(req.getCategoryId() != null, BlogArticle::getCategoryId, req.getCategoryId());
        wrapper.eq(req.getColumnId() != null, BlogArticle::getColumnId, req.getColumnId());
        wrapper.and(StrUtil.isNotBlank(req.getKeyword()), item -> item
                .like(BlogArticle::getTitle, req.getKeyword())
                .or()
                .like(BlogArticle::getSummary, req.getKeyword())
                .or()
                .like(BlogArticle::getSlug, req.getKeyword()));
        if (req.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                            .eq(BlogArticleTag::getTagId, req.getTagId()))
                    .stream()
                    .map(BlogArticleTag::getArticleId)
                    .toList();
            wrapper.in(CollUtil.isNotEmpty(articleIds), BlogArticle::getId, articleIds);
            wrapper.eq(CollUtil.isEmpty(articleIds), BlogArticle::getId, -1L);
        }
        wrapper.orderByDesc(BlogArticle::getTopFlag)
                .orderByDesc(BlogArticle::getPublishTime)
                .orderByDesc(BlogArticle::getUpdateTime);
        return wrapper;
    }

    private LambdaQueryWrapper<BlogArticle> buildPublicArticleSearchQuery(String keyword, String sort) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode());
        wrapper.eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode());
        wrapper.and(item -> item
                .like(BlogArticle::getTitle, keyword)
                .or()
                .like(BlogArticle::getSummary, keyword)
                .or()
                .like(BlogArticle::getContent, keyword)
                .or()
                .like(BlogArticle::getSlug, keyword));
        if ("views".equals(sort)) {
            wrapper.orderByDesc(BlogArticle::getViewCount);
        }
        wrapper.orderByDesc(BlogArticle::getPublishTime)
                .orderByDesc(BlogArticle::getUpdateTime);
        return wrapper;
    }

    private BlogArticle getArticleOrThrow(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BaseException(404, "文章不存在");
        }
        return article;
    }

    private BlogArticle getPublicArticleBySlugOrThrow(String slug) {
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, normalizeSlug(slug))
                .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                .last("LIMIT 1"));
        if (article == null) {
            throw new BaseException(404, "文章不存在或未公开");
        }
        return article;
    }

    private int validatePublicListLimit(Integer limit, int defaultLimit) {
        int value = limit == null ? defaultLimit : limit;
        if (value < 1 || value > 12) {
            throw new BaseException(400, "文章列表数量必须在1到12之间");
        }
        return value;
    }

    private LambdaQueryWrapper<BlogArticle> selectArticleListFields(LambdaQueryWrapper<BlogArticle> wrapper) {
        return wrapper.select(
                BlogArticle::getId,
                BlogArticle::getTitle,
                BlogArticle::getSlug,
                BlogArticle::getSummary,
                BlogArticle::getCoverUrl,
                BlogArticle::getCategoryId,
                BlogArticle::getColumnId,
                BlogArticle::getTopFlag,
                BlogArticle::getVisibility,
                BlogArticle::getStatus,
                BlogArticle::getViewCount,
                BlogArticle::getLikeCount,
                BlogArticle::getPublishTime,
                BlogArticle::getCreateTime,
                BlogArticle::getUpdateTime);
    }

    private Set<Long> listArticleTagIds(Long articleId) {
        return articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .eq(BlogArticleTag::getArticleId, articleId))
                .stream()
                .map(BlogArticleTag::getTagId)
                .collect(Collectors.toSet());
    }

    private Set<Long> listArticleIdsByTagIds(Set<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptySet();
        }
        return articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .in(BlogArticleTag::getTagId, tagIds))
                .stream()
                .map(BlogArticleTag::getArticleId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Set<Long>> listArticleTagIds(List<Long> articleIds) {
        if (CollUtil.isEmpty(articleIds)) {
            return Collections.emptyMap();
        }
        return articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .in(BlogArticleTag::getArticleId, articleIds))
                .stream()
                .collect(Collectors.groupingBy(
                        BlogArticleTag::getArticleId,
                        Collectors.mapping(BlogArticleTag::getTagId, Collectors.toSet())));
    }

    private int compareRelatedArticle(
            BlogArticle current,
            Set<Long> currentTagIds,
            Map<Long, Set<Long>> candidateTagIds,
            BlogArticle left,
            BlogArticle right) {
        int scoreComparison = Integer.compare(
                relatedArticleScore(current, currentTagIds, candidateTagIds.getOrDefault(right.getId(), Collections.emptySet()), right),
                relatedArticleScore(current, currentTagIds, candidateTagIds.getOrDefault(left.getId(), Collections.emptySet()), left));
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        int viewComparison = Long.compare(defaultLong(right.getViewCount()), defaultLong(left.getViewCount()));
        if (viewComparison != 0) {
            return viewComparison;
        }
        return resolveArchiveTime(right).compareTo(resolveArchiveTime(left));
    }

    private int relatedArticleScore(
            BlogArticle current,
            Set<Long> currentTagIds,
            Set<Long> candidateTagIds,
            BlogArticle candidate) {
        int score = 0;
        if (current.getColumnId() != null && Objects.equals(current.getColumnId(), candidate.getColumnId())) {
            score += 6;
        }
        if (current.getCategoryId() != null && Objects.equals(current.getCategoryId(), candidate.getCategoryId())) {
            score += 4;
        }
        score += (int) currentTagIds.stream().filter(candidateTagIds::contains).count() * 2;
        return score;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private List<Long> normalizeBatchArticleIds(List<Long> articleIds) {
        if (CollUtil.isEmpty(articleIds)) {
            throw new BaseException(400, "请选择文章");
        }
        List<Long> distinctIds = articleIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        if (CollUtil.isEmpty(distinctIds)) {
            throw new BaseException(400, "请选择文章");
        }
        if (distinctIds.size() > 100) {
            throw new BaseException(400, "单次最多操作100篇文章");
        }
        return distinctIds;
    }

    private void deleteArticleInternal(BlogArticle article) {
        Long articleId = article.getId();
        List<Long> tagIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .eq(BlogArticleTag::getArticleId, articleId))
                .stream()
                .map(BlogArticleTag::getTagId)
                .toList();
        recycleService.recordArticleDeletion(article, tagIds);
        articleMapper.deleteById(articleId);
        articleTagMapper.delete(new LambdaQueryWrapper<BlogArticleTag>().eq(BlogArticleTag::getArticleId, articleId));
        mediaService.refreshArticleMediaQuotes(article.getCoverUrl(), article.getContent(), null, null);
    }

    private BlogArticleVersion getArticleVersionOrThrow(Long articleId, Long versionId) {
        BlogArticleVersion version = articleVersionMapper.selectOne(new LambdaQueryWrapper<BlogArticleVersion>()
                .eq(BlogArticleVersion::getId, versionId)
                .eq(BlogArticleVersion::getArticleId, articleId)
                .last("LIMIT 1"));
        if (version == null) {
            throw new BaseException(404, "文章历史版本不存在");
        }
        return version;
    }

    private void validateArticleReq(ArticleSaveReq req) {
        validateBinaryFlag(req.getTopFlag(), "置顶状态");
        if (!ArticleStatusEnum.contains(req.getStatus())) {
            throw new BaseException(400, "文章状态不正确");
        }
        if (!ArticleVisibilityEnum.contains(req.getVisibility())) {
            throw new BaseException(400, "可见性状态不正确");
        }
        if (req.getCategoryId() != null && categoryMapper.selectById(req.getCategoryId()) == null) {
            throw new BaseException(400, "分类不存在");
        }
        if (req.getColumnId() != null && columnMapper.selectById(req.getColumnId()) == null) {
            throw new BaseException(400, "专栏不存在");
        }
        if (CollUtil.isNotEmpty(req.getTagIds())) {
            Long count = tagMapper.selectCount(new LambdaQueryWrapper<BlogTag>().in(BlogTag::getId, req.getTagIds()));
            if (count != req.getTagIds().stream().distinct().count()) {
                throw new BaseException(400, "标签不存在");
            }
        }
    }

    private void validateBinaryFlag(Integer value, String fieldName) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BaseException(400, fieldName + "不正确");
        }
    }

    private void ensureSlugUnique(Long id, String slug) {
        Long count = articleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, normalizeSlug(slug))
                .ne(id != null, BlogArticle::getId, id));
        if (count > 0) {
            throw new BaseException(400, "文章访问标识已存在");
        }
    }

    private void fillArticle(BlogArticle article, ArticleSaveReq req) {
        article.setTitle(req.getTitle().trim());
        article.setSlug(normalizeSlug(req.getSlug()));
        article.setSummary(StrUtil.blankToDefault(req.getSummary(), null));
        article.setContent(req.getContent());
        article.setCoverUrl(StrUtil.blankToDefault(req.getCoverUrl(), null));
        article.setCategoryId(req.getCategoryId());
        article.setColumnId(req.getColumnId());
        article.setTopFlag(req.getTopFlag());
        article.setVisibility(req.getVisibility());
        article.setStatus(req.getStatus());
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.delete(new LambdaQueryWrapper<BlogArticleTag>().eq(BlogArticleTag::getArticleId, articleId));
        if (CollUtil.isEmpty(tagIds)) {
            return;
        }
        for (Long tagId : tagIds.stream().distinct().toList()) {
            BlogArticleTag relation = new BlogArticleTag();
            relation.setArticleId(articleId);
            relation.setTagId(tagId);
            articleTagMapper.insert(relation);
        }
    }

    private void syncArticleColumnRelation(Long articleId, Long oldColumnId, Long newColumnId) {
        if (Objects.equals(oldColumnId, newColumnId)) {
            if (newColumnId == null) {
                columnArticleMapper.delete(new LambdaQueryWrapper<BlogColumnArticle>()
                        .eq(BlogColumnArticle::getArticleId, articleId));
                return;
            }
            ensureArticleColumnRelation(articleId, newColumnId);
            return;
        }
        columnArticleMapper.delete(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getArticleId, articleId));
        articleMapper.updateColumnId(articleId, newColumnId, currentUserId());
        if (newColumnId != null) {
            insertArticleColumnRelation(articleId, newColumnId);
        }
    }

    private void ensureArticleColumnRelation(Long articleId, Long columnId) {
        Long relationCount = columnArticleMapper.selectCount(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getArticleId, articleId)
                .eq(BlogColumnArticle::getColumnId, columnId));
        if (relationCount == 0) {
            insertArticleColumnRelation(articleId, columnId);
        }
    }

    private void insertArticleColumnRelation(Long articleId, Long columnId) {
        BlogColumnArticle relation = new BlogColumnArticle();
        relation.setArticleId(articleId);
        relation.setColumnId(columnId);
        relation.setSortOrder(columnArticleMapper.selectMaxSortOrder(columnId) + 1);
        columnArticleMapper.insert(relation);
    }

    private void createVersionSnapshot(BlogArticle article, String remark) {
        BlogArticleVersion version = new BlogArticleVersion();
        version.setArticleId(article.getId());
        version.setTitle(article.getTitle());
        version.setSummary(article.getSummary());
        version.setContent(article.getContent());
        version.setVersionNo(articleVersionMapper.selectMaxVersionNo(article.getId()) + 1);
        version.setVersionRemark(remark);
        version.setCreateBy(currentUserId());
        articleVersionMapper.insert(version);
    }

    private ArticleResp toArticleResp(BlogArticle article) {
        ArticleResp resp = new ArticleResp();
        fillArticleListResp(resp, article, buildArticleRenderContext(List.of(article)));
        resp.setContent(article.getContent());
        return resp;
    }

    private ArticleListResp toArticleListResp(BlogArticle article) {
        return toArticleListResp(article, buildArticleRenderContext(List.of(article)));
    }

    private List<ArticleListResp> toArticleListRespList(List<BlogArticle> articles) {
        if (CollUtil.isEmpty(articles)) {
            return Collections.emptyList();
        }
        ArticleRenderContext context = buildArticleRenderContext(articles);
        return articles.stream().map(article -> toArticleListResp(article, context)).toList();
    }

    private ArticleListResp toArticleListResp(BlogArticle article, ArticleRenderContext context) {
        ArticleListResp resp = new ArticleListResp();
        fillArticleListResp(resp, article, context);
        return resp;
    }

    private void fillArticleListResp(ArticleListResp resp, BlogArticle article, ArticleRenderContext context) {
        resp.setId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSlug(article.getSlug());
        resp.setSummary(article.getSummary());
        resp.setCoverUrl(article.getCoverUrl());
        resp.setCategoryId(article.getCategoryId());
        resp.setCategoryName(context.categoryNameMap().get(article.getCategoryId()));
        resp.setColumnId(article.getColumnId());
        BlogColumn column = context.columnMap().get(article.getColumnId());
        resp.setColumnName(column == null ? null : column.getName());
        resp.setColumnSlug(column == null ? null : column.getSlug());
        List<BlogTag> tags = context.articleTagsMap().getOrDefault(article.getId(), Collections.emptyList());
        resp.setTagIds(tags.stream().map(BlogTag::getId).toList());
        resp.setTagNames(tags.stream().map(BlogTag::getName).toList());
        resp.setTopFlag(article.getTopFlag());
        resp.setVisibility(article.getVisibility());
        resp.setStatus(article.getStatus());
        resp.setViewCount(article.getViewCount());
        resp.setLikeCount(article.getLikeCount());
        resp.setPublishTime(article.getPublishTime());
        resp.setCreateTime(article.getCreateTime());
        resp.setUpdateTime(article.getUpdateTime());
    }

    private ArticleSearchResp toArticleSearchResp(BlogArticle article, String keyword, ArticleRenderContext context) {
        ArticleSearchResp resp = new ArticleSearchResp();
        resp.setId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSlug(article.getSlug());
        resp.setSummary(article.getSummary());
        resp.setSnippet(buildSearchSnippet(article, keyword));
        resp.setCategoryName(context.categoryNameMap().get(article.getCategoryId()));
        BlogColumn column = context.columnMap().get(article.getColumnId());
        resp.setColumnName(column == null ? null : column.getName());
        resp.setTagNames(context.articleTagsMap().getOrDefault(article.getId(), Collections.emptyList())
                .stream()
                .map(BlogTag::getName)
                .toList());
        resp.setViewCount(article.getViewCount());
        resp.setPublishTime(article.getPublishTime());
        resp.setCreateTime(article.getCreateTime());
        return resp;
    }

    private String buildSearchSnippet(BlogArticle article, String keyword) {
        String source = firstMatchedText(keyword, article.getSummary(), article.getContent(), article.getTitle());
        if (StrUtil.isBlank(source)) {
            return StrUtil.blankToDefault(article.getSummary(), article.getTitle());
        }
        String normalized = source.replaceAll("\\s+", " ").trim();
        String lowerSource = normalized.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int index = lowerSource.indexOf(lowerKeyword);
        if (index < 0) {
            return normalized.length() <= 160 ? normalized : normalized.substring(0, 160) + "...";
        }
        int start = Math.max(0, index - 60);
        int end = Math.min(normalized.length(), index + lowerKeyword.length() + 100);
        String prefix = start > 0 ? "..." : "";
        String suffix = end < normalized.length() ? "..." : "";
        return prefix + normalized.substring(start, end) + suffix;
    }

    private String firstMatchedText(String keyword, String... values) {
        String lowerKeyword = keyword.toLowerCase();
        for (String value : values) {
            if (StrUtil.isNotBlank(value) && value.toLowerCase().contains(lowerKeyword)) {
                return value;
            }
        }
        return "";
    }

    private ArticleVersionResp toArticleVersionResp(BlogArticleVersion version) {
        ArticleVersionResp resp = new ArticleVersionResp();
        resp.setId(version.getId());
        resp.setArticleId(version.getArticleId());
        resp.setTitle(version.getTitle());
        resp.setSummary(version.getSummary());
        resp.setVersionNo(version.getVersionNo());
        resp.setVersionRemark(version.getVersionRemark());
        resp.setCreateBy(version.getCreateBy());
        resp.setCreateTime(version.getCreateTime());
        return resp;
    }

    private ArticleVersionDetailResp toArticleVersionDetailResp(BlogArticleVersion version) {
        ArticleVersionDetailResp resp = new ArticleVersionDetailResp();
        resp.setId(version.getId());
        resp.setArticleId(version.getArticleId());
        resp.setTitle(version.getTitle());
        resp.setSummary(version.getSummary());
        resp.setContent(version.getContent());
        resp.setVersionNo(version.getVersionNo());
        resp.setVersionRemark(version.getVersionRemark());
        resp.setCreateBy(version.getCreateBy());
        resp.setCreateTime(version.getCreateTime());
        return resp;
    }

    private ArticleRenderContext buildArticleRenderContext(List<BlogArticle> articles) {
        if (CollUtil.isEmpty(articles)) {
            return new ArticleRenderContext(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }
        List<Long> articleIds = articles.stream()
                .map(BlogArticle::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> columnIds = articles.stream()
                .map(BlogArticle::getColumnId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, String> categoryNameMap = CollUtil.isEmpty(categoryIds)
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(BlogCategory::getId, BlogCategory::getName, (oldValue, newValue) -> oldValue));
        Map<Long, BlogColumn> columnMap = CollUtil.isEmpty(columnIds)
                ? Collections.emptyMap()
                : columnMapper.selectBatchIds(columnIds).stream()
                .collect(Collectors.toMap(BlogColumn::getId, Function.identity(), (oldValue, newValue) -> oldValue));

        if (CollUtil.isEmpty(articleIds)) {
            return new ArticleRenderContext(categoryNameMap, columnMap, Collections.emptyMap());
        }
        List<BlogArticleTag> relations = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .in(BlogArticleTag::getArticleId, articleIds)
                .orderByAsc(BlogArticleTag::getArticleId)
                .orderByAsc(BlogArticleTag::getId));
        if (CollUtil.isEmpty(relations)) {
            return new ArticleRenderContext(categoryNameMap, columnMap, Collections.emptyMap());
        }
        Set<Long> tagIds = relations.stream()
                .map(BlogArticleTag::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, BlogTag> tagMap = CollUtil.isEmpty(tagIds)
                ? Collections.emptyMap()
                : tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(BlogTag::getId, Function.identity(), (oldValue, newValue) -> oldValue));
        Map<Long, List<BlogTag>> articleTagsMap = new LinkedHashMap<>();
        for (BlogArticleTag relation : relations) {
            BlogTag tag = tagMap.get(relation.getTagId());
            if (tag != null) {
                articleTagsMap.computeIfAbsent(relation.getArticleId(), key -> new ArrayList<>()).add(tag);
            }
        }
        return new ArticleRenderContext(categoryNameMap, columnMap, articleTagsMap);
    }

    private record ArticleRenderContext(
            Map<Long, String> categoryNameMap,
            Map<Long, BlogColumn> columnMap,
            Map<Long, List<BlogTag>> articleTagsMap
    ) {
    }

    private LocalDateTime resolveArchiveTime(BlogArticle article) {
        if (article.getPublishTime() != null) {
            return article.getPublishTime();
        }
        if (article.getUpdateTime() != null) {
            return article.getUpdateTime();
        }
        return article.getCreateTime() == null ? LocalDateTime.now() : article.getCreateTime();
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
