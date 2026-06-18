package com.technote.blog.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogArticleTag;
import com.technote.blog.entity.BlogCategory;
import com.technote.blog.entity.BlogColumn;
import com.technote.blog.entity.BlogTag;
import com.technote.blog.enums.ArticleStatusEnum;
import com.technote.blog.enums.ArticleVisibilityEnum;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogArticleTagMapper;
import com.technote.blog.mapper.BlogCategoryMapper;
import com.technote.blog.mapper.BlogColumnMapper;
import com.technote.blog.mapper.BlogTagMapper;
import com.technote.blog.model.resp.ArticleExportFileResp;
import com.technote.blog.service.ArticleExportService;
import com.technote.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章导出服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleExportServiceImpl implements ArticleExportService {

    private static final int MAX_BATCH_EXPORT_SIZE = 100;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BlogArticleMapper articleMapper;

    private final BlogArticleTagMapper articleTagMapper;

    private final BlogCategoryMapper categoryMapper;

    private final BlogColumnMapper columnMapper;

    private final BlogTagMapper tagMapper;

    @Override
    public ArticleExportFileResp exportArticle(Long id) {
        BlogArticle article = getArticleOrThrow(id);
        ArticleExportFileResp resp = new ArticleExportFileResp();
        resp.setFileName(buildFileName(article));
        resp.setContent(buildMarkdown(article));
        return resp;
    }

    @Override
    public ArticleExportFileResp exportArticles(List<Long> articleIds) {
        if (CollUtil.isEmpty(articleIds)) {
            throw new BaseException(400, "请选择要导出的文章");
        }
        List<Long> distinctIds = articleIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        if (CollUtil.isEmpty(distinctIds)) {
            throw new BaseException(400, "请选择要导出的文章");
        }
        if (distinctIds.size() > MAX_BATCH_EXPORT_SIZE) {
            throw new BaseException(400, "单次最多导出100篇文章");
        }
        List<BlogArticle> articles = distinctIds.stream().map(this::getArticleOrThrow).toList();
        ArticleExportFileResp resp = new ArticleExportFileResp();
        resp.setFileName("technote-articles-" + System.currentTimeMillis() + ".md");
        resp.setContent(articles.stream().map(this::buildMarkdown).collect(Collectors.joining("\n\n---\n\n")));
        return resp;
    }

    private BlogArticle getArticleOrThrow(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BaseException(404, "文章不存在");
        }
        return article;
    }

    private String buildMarkdown(BlogArticle article) {
        List<String> tagNames = listTagNames(article.getId());
        StringBuilder builder = new StringBuilder();
        builder.append("---\n");
        appendMeta(builder, "title", article.getTitle());
        appendMeta(builder, "slug", article.getSlug());
        appendMeta(builder, "status", resolveStatus(article.getStatus()));
        appendMeta(builder, "visibility", resolveVisibility(article.getVisibility()));
        appendMeta(builder, "category", resolveCategoryName(article.getCategoryId()));
        appendMeta(builder, "column", resolveColumnName(article.getColumnId()));
        appendMeta(builder, "tags", String.join(", ", tagNames));
        appendMeta(builder, "publishTime", article.getPublishTime() == null ? null : DATE_TIME_FORMATTER.format(article.getPublishTime()));
        appendMeta(builder, "updateTime", article.getUpdateTime() == null ? null : DATE_TIME_FORMATTER.format(article.getUpdateTime()));
        builder.append("---\n\n");
        builder.append("# ").append(StrUtil.blankToDefault(article.getTitle(), "未命名文章")).append("\n\n");
        if (StrUtil.isNotBlank(article.getSummary())) {
            builder.append("> ").append(article.getSummary().trim().replace("\n", "\n> ")).append("\n\n");
        }
        builder.append(StrUtil.blankToDefault(article.getContent(), "")).append("\n");
        return builder.toString();
    }

    private void appendMeta(StringBuilder builder, String key, String value) {
        builder.append(key).append(": ").append(StrUtil.blankToDefault(value, "")).append("\n");
    }

    private List<String> listTagNames(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .eq(BlogArticleTag::getArticleId, articleId))
                .stream()
                .map(BlogArticleTag::getTagId)
                .toList();
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        Map<Long, BlogTag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(BlogTag::getId, Function.identity()));
        return tagIds.stream()
                .map(tagMap::get)
                .filter(tag -> tag != null)
                .map(BlogTag::getName)
                .toList();
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        BlogCategory category = categoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    private String resolveColumnName(Long columnId) {
        if (columnId == null) {
            return null;
        }
        BlogColumn column = columnMapper.selectById(columnId);
        return column == null ? null : column.getName();
    }

    private String resolveStatus(Integer status) {
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(status)) {
            return "published";
        }
        if (ArticleStatusEnum.AUTO_DRAFT.getCode().equals(status)) {
            return "auto_draft";
        }
        return "draft";
    }

    private String resolveVisibility(Integer visibility) {
        return ArticleVisibilityEnum.PUBLIC.getCode().equals(visibility) ? "public" : "private";
    }

    private String buildFileName(BlogArticle article) {
        String baseName = StrUtil.blankToDefault(article.getSlug(), article.getTitle());
        String normalized = StrUtil.blankToDefault(baseName, "article")
                .trim()
                .replaceAll("[\\\\/:*?\"<>|\\s]+", "-")
                .replaceAll("^-+|-+$", "");
        return StrUtil.blankToDefault(normalized, "article") + ".md";
    }
}
