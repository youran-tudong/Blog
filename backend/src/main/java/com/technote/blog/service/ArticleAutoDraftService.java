package com.technote.blog.service;

import com.technote.blog.model.req.ArticleAutoDraftCleanupReq;
import com.technote.blog.model.req.ArticleAutoDraftPageQueryReq;
import com.technote.blog.model.req.ArticleAutoDraftSaveReq;
import com.technote.blog.model.resp.ArticleAutoDraftCleanupResp;
import com.technote.blog.model.resp.ArticleAutoDraftListResp;
import com.technote.blog.model.resp.ArticleAutoDraftResp;
import com.technote.common.model.PageResp;

/**
 * 文章自动草稿服务。
 */
public interface ArticleAutoDraftService {

    /**
     * 分页查询后台自动草稿维护列表。
     *
     * @param req 查询条件
     * @return 自动草稿分页结果
     */
    PageResp<ArticleAutoDraftListResp> pageAutoDrafts(ArticleAutoDraftPageQueryReq req);

    /**
     * 保存或覆盖当前用户的自动草稿。
     *
     * @param req 自动草稿内容
     * @return 自动草稿信息
     */
    ArticleAutoDraftResp saveAutoDraft(ArticleAutoDraftSaveReq req);

    /**
     * 根据草稿键查询当前用户的自动草稿。
     *
     * @param draftKey 草稿键
     * @return 自动草稿信息，不存在时返回 null
     */
    ArticleAutoDraftResp getAutoDraftByKey(String draftKey);

    /**
     * 查询当前用户编辑某篇文章时最近保存的自动草稿。
     *
     * @param articleId 文章ID
     * @return 自动草稿信息，不存在时返回 null
     */
    ArticleAutoDraftResp getAutoDraftByArticle(Long articleId);

    /**
     * 删除当前用户指定自动草稿。
     *
     * @param draftKey 草稿键
     */
    void deleteAutoDraftByKey(String draftKey);

    /**
     * 后台按ID物理删除指定自动草稿。
     *
     * @param id 草稿ID
     */
    void deleteAutoDraftById(Long id);

    /**
     * 清理超过保留天数的自动草稿。
     *
     * @param req 清理条件
     * @return 清理结果
     */
    ArticleAutoDraftCleanupResp cleanupExpiredAutoDrafts(ArticleAutoDraftCleanupReq req);
}
