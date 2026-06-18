package com.technote.blog.service;

import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.AuditReq;
import com.technote.blog.model.req.CommentSubmitReq;
import com.technote.blog.model.resp.CommentResp;
import com.technote.blog.model.resp.PublicCommentResp;
import com.technote.common.model.PageResp;

import java.util.List;

/**
 * 文章评论服务。
 */
public interface CommentService {

    /**
     * 查询文章前台已通过评论，并按一级评论和回复构建两层结构。
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<PublicCommentResp> listApprovedComments(Long articleId);

    /**
     * 提交评论，默认进入待审核状态。
     *
     * @param req 评论提交请求
     * @return 评论信息
     */
    PublicCommentResp submitComment(CommentSubmitReq req);

    /**
     * 分页查询后台评论审核列表。
     *
     * @param req 查询条件
     * @return 评论分页结果
     */
    PageResp<CommentResp> pageAdminComments(AuditPageQueryReq req);

    /**
     * 审核评论。
     *
     * @param id  评论ID
     * @param req 审核请求
     */
    void auditComment(Long id, AuditReq req);

    /**
     * 删除评论。
     *
     * @param id 评论ID
     */
    void deleteComment(Long id);
}
