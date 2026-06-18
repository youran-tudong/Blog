package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogArticleAutoDraft;
import com.technote.blog.entity.BlogTag;
import com.technote.blog.enums.ArticleVisibilityEnum;
import com.technote.blog.mapper.BlogArticleAutoDraftMapper;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogCategoryMapper;
import com.technote.blog.mapper.BlogColumnMapper;
import com.technote.blog.mapper.BlogTagMapper;
import com.technote.blog.model.req.ArticleAutoDraftCleanupReq;
import com.technote.blog.model.req.ArticleAutoDraftPageQueryReq;
import com.technote.blog.model.req.ArticleAutoDraftSaveReq;
import com.technote.blog.model.resp.ArticleAutoDraftCleanupResp;
import com.technote.blog.model.resp.ArticleAutoDraftListResp;
import com.technote.blog.model.resp.ArticleAutoDraftResp;
import com.technote.blog.service.ArticleAutoDraftService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 文章自动草稿服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleAutoDraftServiceImpl implements ArticleAutoDraftService {

    private final BlogArticleAutoDraftMapper autoDraftMapper;

    private final BlogArticleMapper articleMapper;

    private final BlogCategoryMapper categoryMapper;

    private final BlogColumnMapper columnMapper;

    private final BlogTagMapper tagMapper;

    private final ObjectMapper objectMapper;

    @Override
    public PageResp<ArticleAutoDraftListResp> pageAutoDrafts(ArticleAutoDraftPageQueryReq req) {
        IPage<BlogArticleAutoDraft> page = autoDraftMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildAutoDraftQuery(req));
        List<ArticleAutoDraftListResp> records = page.getRecords().stream()
                .map(this::toAutoDraftListResp)
                .toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleAutoDraftResp saveAutoDraft(ArticleAutoDraftSaveReq req) {
        validateAutoDraftReq(req);
        Long userId = currentUserId();
        BlogArticleAutoDraft draft = autoDraftMapper.selectOne(new LambdaQueryWrapper<BlogArticleAutoDraft>()
                .eq(BlogArticleAutoDraft::getDraftKey, normalizeDraftKey(req.getDraftKey()))
                .eq(BlogArticleAutoDraft::getCreateBy, userId)
                .last("LIMIT 1"));
        if (draft == null) {
            draft = new BlogArticleAutoDraft();
            draft.setDraftKey(normalizeDraftKey(req.getDraftKey()));
            draft.setCreateBy(userId);
        }
        fillAutoDraft(draft, req, userId);
        if (draft.getId() == null) {
            autoDraftMapper.insert(draft);
        } else {
            autoDraftMapper.updateById(draft);
        }
        return toAutoDraftResp(draft);
    }

    @Override
    public ArticleAutoDraftResp getAutoDraftByKey(String draftKey) {
        if (StrUtil.isBlank(draftKey)) {
            throw new BaseException(400, "草稿键不能为空");
        }
        BlogArticleAutoDraft draft = autoDraftMapper.selectOne(new LambdaQueryWrapper<BlogArticleAutoDraft>()
                .eq(BlogArticleAutoDraft::getDraftKey, normalizeDraftKey(draftKey))
                .eq(BlogArticleAutoDraft::getCreateBy, currentUserId())
                .last("LIMIT 1"));
        return draft == null ? null : toAutoDraftResp(draft);
    }

    @Override
    public ArticleAutoDraftResp getAutoDraftByArticle(Long articleId) {
        BlogArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BaseException(404, "文章不存在");
        }
        BlogArticleAutoDraft draft = autoDraftMapper.selectOne(new LambdaQueryWrapper<BlogArticleAutoDraft>()
                .eq(BlogArticleAutoDraft::getArticleId, articleId)
                .eq(BlogArticleAutoDraft::getCreateBy, currentUserId())
                .orderByDesc(BlogArticleAutoDraft::getUpdateTime)
                .last("LIMIT 1"));
        return draft == null ? null : toAutoDraftResp(draft);
    }

    @Override
    public void deleteAutoDraftByKey(String draftKey) {
        if (StrUtil.isBlank(draftKey)) {
            throw new BaseException(400, "草稿键不能为空");
        }
        autoDraftMapper.hardDeleteByDraftKeyAndCreateBy(normalizeDraftKey(draftKey), currentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoDraftById(Long id) {
        if (id == null) {
            throw new BaseException(400, "自动草稿ID不能为空");
        }
        BlogArticleAutoDraft draft = autoDraftMapper.selectById(id);
        if (draft == null) {
            throw new BaseException(404, "自动草稿不存在");
        }
        autoDraftMapper.hardDeleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleAutoDraftCleanupResp cleanupExpiredAutoDrafts(ArticleAutoDraftCleanupReq req) {
        Integer retentionDays = req.getRetentionDays() == null ? 30 : req.getRetentionDays();
        Boolean onlyOrphan = req.getOnlyOrphan() == null ? Boolean.TRUE : req.getOnlyOrphan();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = autoDraftMapper.hardDeleteExpired(cutoffTime, onlyOrphan);

        ArticleAutoDraftCleanupResp resp = new ArticleAutoDraftCleanupResp();
        resp.setDeletedCount(deletedCount);
        resp.setRetentionDays(retentionDays);
        resp.setOnlyOrphan(onlyOrphan);
        resp.setCutoffTime(cutoffTime);
        return resp;
    }

    private LambdaQueryWrapper<BlogArticleAutoDraft> buildAutoDraftQuery(ArticleAutoDraftPageQueryReq req) {
        LambdaQueryWrapper<BlogArticleAutoDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getArticleId() != null, BlogArticleAutoDraft::getArticleId, req.getArticleId());
        wrapper.eq(req.getCreateBy() != null, BlogArticleAutoDraft::getCreateBy, req.getCreateBy());
        wrapper.isNull(Boolean.TRUE.equals(req.getOnlyOrphan()), BlogArticleAutoDraft::getArticleId);
        wrapper.and(StrUtil.isNotBlank(req.getKeyword()), item -> item
                .like(BlogArticleAutoDraft::getTitle, req.getKeyword())
                .or()
                .like(BlogArticleAutoDraft::getSlug, req.getKeyword())
                .or()
                .like(BlogArticleAutoDraft::getDraftKey, req.getKeyword()));
        wrapper.orderByDesc(BlogArticleAutoDraft::getUpdateTime)
                .orderByDesc(BlogArticleAutoDraft::getId);
        return wrapper;
    }

    private void validateAutoDraftReq(ArticleAutoDraftSaveReq req) {
        if (StrUtil.isBlank(req.getDraftKey())) {
            throw new BaseException(400, "草稿键不能为空");
        }
        if (req.getArticleId() != null && articleMapper.selectById(req.getArticleId()) == null) {
            throw new BaseException(404, "文章不存在");
        }
        if (req.getCategoryId() != null && categoryMapper.selectById(req.getCategoryId()) == null) {
            throw new BaseException(400, "分类不存在");
        }
        if (req.getColumnId() != null && columnMapper.selectById(req.getColumnId()) == null) {
            throw new BaseException(400, "专栏不存在");
        }
        if (req.getTopFlag() != null && req.getTopFlag() != 0 && req.getTopFlag() != 1) {
            throw new BaseException(400, "置顶状态不正确");
        }
        if (req.getVisibility() != null && !ArticleVisibilityEnum.contains(req.getVisibility())) {
            throw new BaseException(400, "可见性状态不正确");
        }
        if (CollUtil.isNotEmpty(req.getTagIds())) {
            Long count = tagMapper.selectCount(new LambdaQueryWrapper<BlogTag>().in(BlogTag::getId, req.getTagIds()));
            if (count != req.getTagIds().stream().distinct().count()) {
                throw new BaseException(400, "标签不存在");
            }
        }
    }

    private void fillAutoDraft(BlogArticleAutoDraft draft, ArticleAutoDraftSaveReq req, Long userId) {
        draft.setArticleId(req.getArticleId());
        draft.setTitle(StrUtil.blankToDefault(req.getTitle(), null));
        draft.setSlug(StrUtil.blankToDefault(req.getSlug(), null));
        draft.setSummary(StrUtil.blankToDefault(req.getSummary(), null));
        draft.setContent(StrUtil.blankToDefault(req.getContent(), ""));
        draft.setCoverUrl(StrUtil.blankToDefault(req.getCoverUrl(), null));
        draft.setCategoryId(req.getCategoryId());
        draft.setColumnId(req.getColumnId());
        draft.setTagIds(writeTagIds(req.getTagIds()));
        draft.setTopFlag(req.getTopFlag() == null ? 0 : req.getTopFlag());
        draft.setVisibility(req.getVisibility() == null ? ArticleVisibilityEnum.PUBLIC.getCode() : req.getVisibility());
        draft.setUpdateBy(userId);
    }

    private String writeTagIds(List<Long> tagIds) {
        List<Long> normalized = CollUtil.isEmpty(tagIds) ? Collections.emptyList() : tagIds.stream().distinct().toList();
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new BaseException(500, "自动草稿标签生成失败");
        }
    }

    private List<Long> readTagIds(String tagIds) {
        if (StrUtil.isBlank(tagIds)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tagIds, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }

    private ArticleAutoDraftResp toAutoDraftResp(BlogArticleAutoDraft draft) {
        ArticleAutoDraftResp resp = new ArticleAutoDraftResp();
        resp.setId(draft.getId());
        resp.setDraftKey(draft.getDraftKey());
        resp.setArticleId(draft.getArticleId());
        resp.setTitle(draft.getTitle());
        resp.setSlug(draft.getSlug());
        resp.setSummary(draft.getSummary());
        resp.setContent(draft.getContent());
        resp.setCoverUrl(draft.getCoverUrl());
        resp.setCategoryId(draft.getCategoryId());
        resp.setColumnId(draft.getColumnId());
        resp.setTagIds(readTagIds(draft.getTagIds()));
        resp.setTopFlag(draft.getTopFlag());
        resp.setVisibility(draft.getVisibility());
        resp.setCreateTime(draft.getCreateTime());
        resp.setUpdateTime(draft.getUpdateTime());
        return resp;
    }

    private ArticleAutoDraftListResp toAutoDraftListResp(BlogArticleAutoDraft draft) {
        ArticleAutoDraftListResp resp = new ArticleAutoDraftListResp();
        resp.setId(draft.getId());
        resp.setDraftKey(draft.getDraftKey());
        resp.setArticleId(draft.getArticleId());
        resp.setTitle(draft.getTitle());
        resp.setSlug(draft.getSlug());
        resp.setSummary(draft.getSummary());
        resp.setContentPreview(buildContentPreview(draft.getContent()));
        resp.setContentLength(draft.getContent() == null ? 0 : draft.getContent().length());
        resp.setCategoryId(draft.getCategoryId());
        resp.setColumnId(draft.getColumnId());
        resp.setTopFlag(draft.getTopFlag());
        resp.setVisibility(draft.getVisibility());
        resp.setCreateBy(draft.getCreateBy());
        resp.setCreateTime(draft.getCreateTime());
        resp.setUpdateBy(draft.getUpdateBy());
        resp.setUpdateTime(draft.getUpdateTime());
        return resp;
    }

    private String buildContentPreview(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 120) + "...";
    }

    private String normalizeDraftKey(String draftKey) {
        return draftKey.trim();
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
