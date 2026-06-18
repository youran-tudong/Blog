package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogComment;
import com.technote.blog.enums.ArticleStatusEnum;
import com.technote.blog.enums.ArticleVisibilityEnum;
import com.technote.blog.enums.AuditStatusEnum;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogCommentMapper;
import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.AuditReq;
import com.technote.blog.model.req.CommentSubmitReq;
import com.technote.blog.model.resp.CommentResp;
import com.technote.blog.model.resp.PublicCommentResp;
import com.technote.blog.service.CommentService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章评论服务实现。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final BlogCommentMapper commentMapper;

    private final BlogArticleMapper articleMapper;

    @Override
    public List<PublicCommentResp> listApprovedComments(Long articleId) {
        ensurePublicArticle(articleId);
        List<BlogComment> comments = commentMapper.selectList(new LambdaQueryWrapper<BlogComment>()
                        .eq(BlogComment::getArticleId, articleId)
                        .eq(BlogComment::getStatus, AuditStatusEnum.APPROVED.getCode())
                        .orderByAsc(BlogComment::getCreateTime));
        return buildPublicCommentTree(comments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicCommentResp submitComment(CommentSubmitReq req) {
        ensurePublicArticle(req.getArticleId());
        Long parentId = req.getParentId() == null ? 0L : req.getParentId();
        ensureReplyableParent(req.getArticleId(), parentId);
        BlogComment comment = new BlogComment();
        comment.setArticleId(req.getArticleId());
        comment.setParentId(parentId);
        comment.setNickname(req.getNickname().trim());
        comment.setEmail(StrUtil.blankToDefault(req.getEmail(), null));
        comment.setWebsite(StrUtil.blankToDefault(req.getWebsite(), null));
        comment.setContent(req.getContent().trim());
        comment.setStatus(AuditStatusEnum.PENDING.getCode());
        commentMapper.insert(comment);
        return toPublicCommentResp(comment);
    }

    @Override
    public PageResp<CommentResp> pageAdminComments(AuditPageQueryReq req) {
        IPage<BlogComment> page = commentMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                new LambdaQueryWrapper<BlogComment>()
                        .eq(req.getStatus() != null, BlogComment::getStatus, req.getStatus())
                        .eq(req.getArticleId() != null, BlogComment::getArticleId, req.getArticleId())
                        .orderByAsc(BlogComment::getStatus)
                        .orderByDesc(BlogComment::getCreateTime));
        List<CommentResp> records = page.getRecords().stream().map(this::toCommentResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long id, AuditReq req) {
        if (!AuditStatusEnum.contains(req.getStatus()) || AuditStatusEnum.PENDING.getCode().equals(req.getStatus())) {
            throw new BaseException(400, "审核状态不正确");
        }
        BlogComment comment = getCommentOrThrow(id);
        LambdaUpdateWrapper<BlogComment> wrapper = new LambdaUpdateWrapper<BlogComment>()
                .eq(BlogComment::getId, comment.getId())
                .eq(BlogComment::getStatus, AuditStatusEnum.PENDING.getCode())
                .set(BlogComment::getStatus, req.getStatus())
                .set(BlogComment::getReplyContent, StrUtil.blankToDefault(req.getReplyContent(), null))
                .set(BlogComment::getAuditBy, currentUserId())
                .set(BlogComment::getAuditTime, LocalDateTime.now());
        if (commentMapper.update(null, wrapper) == 0) {
            throw new BaseException(409, "评论已审核，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        BlogComment comment = getCommentForUpdateOrThrow(id);
        if (Long.valueOf(0L).equals(comment.getParentId())) {
            commentMapper.delete(new LambdaQueryWrapper<BlogComment>().eq(BlogComment::getParentId, id));
        }
        commentMapper.deleteById(id);
    }

    private BlogComment getCommentOrThrow(Long id) {
        BlogComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BaseException(404, "评论不存在");
        }
        return comment;
    }

    private BlogComment getCommentForUpdateOrThrow(Long id) {
        BlogComment comment = commentMapper.selectOne(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getId, id)
                .last("LIMIT 1 FOR UPDATE"));
        if (comment == null) {
            throw new BaseException(404, "评论不存在");
        }
        return comment;
    }

    private void ensurePublicArticle(Long articleId) {
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getId, articleId)
                .eq(BlogArticle::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getVisibility, ArticleVisibilityEnum.PUBLIC.getCode())
                .last("LIMIT 1"));
        if (article == null) {
            throw new BaseException(404, "文章不存在或未公开");
        }
    }

    private void ensureReplyableParent(Long articleId, Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        BlogComment parent = commentMapper.selectOne(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getId, parentId)
                .eq(BlogComment::getArticleId, articleId)
                .eq(BlogComment::getParentId, 0L)
                .eq(BlogComment::getStatus, AuditStatusEnum.APPROVED.getCode())
                .last("LIMIT 1 FOR UPDATE"));
        if (parent == null) {
            throw new BaseException(400, "父评论不存在、未通过审核或不可回复");
        }
    }

    private List<PublicCommentResp> buildPublicCommentTree(List<BlogComment> comments) {
        Map<Long, PublicCommentResp> topLevelMap = new LinkedHashMap<>();
        for (BlogComment comment : comments) {
            if (Long.valueOf(0L).equals(comment.getParentId())) {
                topLevelMap.put(comment.getId(), toPublicCommentResp(comment));
            }
        }
        for (BlogComment comment : comments) {
            if (Long.valueOf(0L).equals(comment.getParentId())) {
                continue;
            }
            PublicCommentResp parent = topLevelMap.get(comment.getParentId());
            if (parent != null) {
                parent.getReplies().add(toPublicCommentResp(comment));
            }
        }
        return List.copyOf(topLevelMap.values());
    }

    private PublicCommentResp toPublicCommentResp(BlogComment comment) {
        PublicCommentResp resp = new PublicCommentResp();
        resp.setId(comment.getId());
        resp.setArticleId(comment.getArticleId());
        resp.setParentId(comment.getParentId());
        resp.setNickname(comment.getNickname());
        resp.setWebsite(comment.getWebsite());
        resp.setContent(comment.getContent());
        resp.setReplyContent(comment.getReplyContent());
        resp.setCreateTime(comment.getCreateTime());
        return resp;
    }

    private CommentResp toCommentResp(BlogComment comment) {
        CommentResp resp = new CommentResp();
        resp.setId(comment.getId());
        resp.setArticleId(comment.getArticleId());
        resp.setArticleTitle(resolveArticleTitle(comment.getArticleId()));
        resp.setParentId(comment.getParentId());
        resp.setNickname(comment.getNickname());
        resp.setEmail(comment.getEmail());
        resp.setWebsite(comment.getWebsite());
        resp.setContent(comment.getContent());
        resp.setStatus(comment.getStatus());
        resp.setReplyContent(comment.getReplyContent());
        resp.setAuditBy(comment.getAuditBy());
        resp.setAuditTime(comment.getAuditTime());
        resp.setCreateTime(comment.getCreateTime());
        resp.setUpdateTime(comment.getUpdateTime());
        return resp;
    }

    private String resolveArticleTitle(Long articleId) {
        BlogArticle article = articleMapper.selectById(articleId);
        return article == null ? null : article.getTitle();
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
