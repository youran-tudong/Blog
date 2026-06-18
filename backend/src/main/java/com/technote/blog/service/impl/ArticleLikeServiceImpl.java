package com.technote.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.enums.ArticleStatusEnum;
import com.technote.blog.enums.ArticleVisibilityEnum;
import com.technote.blog.mapper.BlogArticleLikeMapper;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.model.resp.ArticleLikeStatusResp;
import com.technote.blog.service.ArticleLikeService;
import com.technote.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文章匿名点赞服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private final BlogArticleMapper articleMapper;

    private final BlogArticleLikeMapper articleLikeMapper;

    @Override
    public ArticleLikeStatusResp getLikeStatus(String slug, String visitorKey) {
        BlogArticle article = getPublicArticleOrThrow(slug);
        return buildStatus(article.getId(), visitorKey, article.getLikeCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleLikeStatusResp likeArticle(String slug, String visitorKey) {
        BlogArticle article = getPublicArticleOrThrow(slug);
        int inserted = articleLikeMapper.insertIgnore(article.getId(), visitorKey);
        if (inserted > 0 && articleMapper.increasePublicLikeCount(article.getId()) == 0) {
            throw new BaseException(409, "文章状态已变化，请刷新后重试");
        }
        return buildCurrentStatus(article.getId(), visitorKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleLikeStatusResp unlikeArticle(String slug, String visitorKey) {
        BlogArticle article = getPublicArticleOrThrow(slug);
        int deleted = articleLikeMapper.deleteByArticleAndVisitor(article.getId(), visitorKey);
        if (deleted > 0 && articleMapper.decreasePublicLikeCount(article.getId()) == 0) {
            throw new BaseException(409, "文章状态已变化，请刷新后重试");
        }
        return buildCurrentStatus(article.getId(), visitorKey);
    }

    private BlogArticle getPublicArticleOrThrow(String slug) {
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .select(BlogArticle::getId, BlogArticle::getLikeCount)
                .eq(BlogArticle::getSlug, slug.trim().toLowerCase())
                .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                .last("LIMIT 1"));
        if (article == null) {
            throw new BaseException(404, "文章不存在或未公开");
        }
        return article;
    }

    private ArticleLikeStatusResp buildCurrentStatus(Long articleId, String visitorKey) {
        Long likeCount = articleMapper.selectPublicLikeCount(articleId);
        if (likeCount == null) {
            throw new BaseException(404, "文章不存在或未公开");
        }
        return buildStatus(articleId, visitorKey, likeCount);
    }

    private ArticleLikeStatusResp buildStatus(Long articleId, String visitorKey, Long likeCount) {
        ArticleLikeStatusResp resp = new ArticleLikeStatusResp();
        resp.setLiked(articleLikeMapper.countByArticleAndVisitor(articleId, visitorKey) > 0);
        resp.setLikeCount(likeCount == null ? 0L : likeCount);
        return resp;
    }
}

