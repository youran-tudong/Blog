package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogArticleTag;
import com.technote.blog.entity.BlogColumnArticle;
import com.technote.blog.entity.BlogRecycle;
import com.technote.blog.enums.RecycleResourceTypeEnum;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogArticleLikeMapper;
import com.technote.blog.mapper.BlogArticleTagMapper;
import com.technote.blog.mapper.BlogArticleViewDailyMapper;
import com.technote.blog.mapper.BlogArticleVersionMapper;
import com.technote.blog.mapper.BlogColumnArticleMapper;
import com.technote.blog.mapper.BlogCommentMapper;
import com.technote.blog.mapper.BlogRecycleMapper;
import com.technote.blog.model.req.RecyclePageQueryReq;
import com.technote.blog.model.resp.RecycleResp;
import com.technote.blog.service.MediaService;
import com.technote.blog.service.RecycleService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 回收站服务实现。
 */
@Service
@RequiredArgsConstructor
public class RecycleServiceImpl implements RecycleService {

    private final BlogRecycleMapper recycleMapper;

    private final BlogArticleMapper articleMapper;

    private final BlogArticleLikeMapper articleLikeMapper;

    private final BlogArticleViewDailyMapper articleViewDailyMapper;

    private final BlogArticleTagMapper articleTagMapper;

    private final BlogArticleVersionMapper articleVersionMapper;

    private final BlogColumnArticleMapper columnArticleMapper;

    private final BlogCommentMapper commentMapper;

    private final MediaService mediaService;

    private final ObjectMapper objectMapper;

    @Override
    public void recordArticleDeletion(BlogArticle article, List<Long> tagIds) {
        if (article == null || article.getId() == null) {
            throw new BaseException(400, "文章不存在");
        }
        String resourceType = RecycleResourceTypeEnum.ARTICLE.getCode();
        if (recycleMapper.countByResource(resourceType, article.getId()) > 0) {
            throw new BaseException(400, "文章已在回收站中");
        }
        BlogRecycle recycle = new BlogRecycle();
        recycle.setResourceType(resourceType);
        recycle.setResourceId(article.getId());
        recycle.setTitle(article.getTitle());
        recycle.setContentSnapshot(writeSnapshot(article, tagIds));
        recycle.setDeleteBy(currentUserId());
        recycle.setDeleteTime(LocalDateTime.now());
        recycleMapper.insert(recycle);
    }

    @Override
    public PageResp<RecycleResp> pageRecycle(RecyclePageQueryReq req) {
        validateResourceType(req.getResourceType());
        IPage<BlogRecycle> page = recycleMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                new LambdaQueryWrapper<BlogRecycle>()
                        .eq(StrUtil.isNotBlank(req.getResourceType()), BlogRecycle::getResourceType, req.getResourceType())
                        .like(StrUtil.isNotBlank(req.getKeyword()), BlogRecycle::getTitle, req.getKeyword())
                        .orderByDesc(BlogRecycle::getDeleteTime));
        List<RecycleResp> records = page.getRecords().stream().map(this::toRecycleResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreRecycle(Long id) {
        BlogRecycle recycle = getRecycleOrThrow(id);
        if (!RecycleResourceTypeEnum.ARTICLE.getCode().equals(recycle.getResourceType())) {
            throw new BaseException(400, "暂不支持恢复该资源类型");
        }
        restoreArticle(recycle);
        recycleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecyclePermanently(Long id) {
        BlogRecycle recycle = getRecycleOrThrow(id);
        if (!RecycleResourceTypeEnum.ARTICLE.getCode().equals(recycle.getResourceType())) {
            throw new BaseException(400, "暂不支持永久删除该资源类型");
        }
        // 文章进入回收站时已经释放媒体引用，永久删除只清理数据库记录，避免误删可能复用的本地图片。
        articleTagMapper.delete(new LambdaQueryWrapper<BlogArticleTag>()
                .eq(BlogArticleTag::getArticleId, recycle.getResourceId()));
        columnArticleMapper.delete(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getArticleId, recycle.getResourceId()));
        articleVersionMapper.hardDeleteByArticleId(recycle.getResourceId());
        commentMapper.hardDeleteByArticleId(recycle.getResourceId());
        articleLikeMapper.hardDeleteByArticleId(recycle.getResourceId());
        articleViewDailyMapper.hardDeleteByArticleId(recycle.getResourceId());
        articleMapper.hardDeleteDeletedById(recycle.getResourceId());
        recycleMapper.deleteById(id);
    }

    private void restoreArticle(BlogRecycle recycle) {
        ArticleRecycleSnapshot snapshot = readSnapshot(recycle.getContentSnapshot());
        BlogArticle deletedArticle = articleMapper.selectDeletedById(recycle.getResourceId());
        if (deletedArticle == null) {
            throw new BaseException(404, "被删除文章不存在，不能恢复");
        }
        Long activeSlugCount = articleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, deletedArticle.getSlug()));
        if (activeSlugCount > 0) {
            throw new BaseException(400, "文章访问标识已被占用，不能恢复");
        }
        if (articleMapper.restoreDeletedById(recycle.getResourceId(), currentUserId()) == 0) {
            throw new BaseException(400, "文章状态已变化，不能恢复");
        }
        restoreArticleTags(recycle.getResourceId(), snapshot.tagIds());
        mediaService.refreshArticleMediaQuotes(null, null, deletedArticle.getCoverUrl(), deletedArticle.getContent());
    }

    private void restoreArticleTags(Long articleId, List<Long> tagIds) {
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

    private String writeSnapshot(BlogArticle article, List<Long> tagIds) {
        ArticleRecycleSnapshot snapshot = new ArticleRecycleSnapshot(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContent(),
                article.getCoverUrl(),
                article.getCategoryId(),
                article.getColumnId(),
                article.getTopFlag(),
                article.getVisibility(),
                article.getStatus(),
                tagIds == null ? Collections.emptyList() : tagIds
        );
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new BaseException(500, "回收站快照生成失败");
        }
    }

    private ArticleRecycleSnapshot readSnapshot(String contentSnapshot) {
        if (StrUtil.isBlank(contentSnapshot)) {
            throw new BaseException(500, "回收站快照为空");
        }
        try {
            return objectMapper.readValue(contentSnapshot, ArticleRecycleSnapshot.class);
        } catch (JsonProcessingException ex) {
            throw new BaseException(500, "回收站快照解析失败");
        }
    }

    private BlogRecycle getRecycleOrThrow(Long id) {
        BlogRecycle recycle = recycleMapper.selectById(id);
        if (recycle == null) {
            throw new BaseException(404, "回收站记录不存在");
        }
        return recycle;
    }

    private void validateResourceType(String resourceType) {
        if (StrUtil.isNotBlank(resourceType) && !RecycleResourceTypeEnum.contains(resourceType)) {
            throw new BaseException(400, "资源类型不正确");
        }
    }

    private RecycleResp toRecycleResp(BlogRecycle recycle) {
        RecycleResp resp = new RecycleResp();
        resp.setId(recycle.getId());
        resp.setResourceType(recycle.getResourceType());
        resp.setResourceId(recycle.getResourceId());
        resp.setTitle(recycle.getTitle());
        resp.setDeleteBy(recycle.getDeleteBy());
        resp.setDeleteTime(recycle.getDeleteTime());
        return resp;
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }

    /**
     * 文章删除快照。当前恢复主要使用 tagIds，其他字段用于后续展示和追溯。
     */
    private record ArticleRecycleSnapshot(
            Long id,
            String title,
            String slug,
            String summary,
            String content,
            String coverUrl,
            Long categoryId,
            Long columnId,
            Integer topFlag,
            Integer visibility,
            Integer status,
            List<Long> tagIds
    ) {
    }
}
