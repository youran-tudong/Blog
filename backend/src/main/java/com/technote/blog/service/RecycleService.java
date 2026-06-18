package com.technote.blog.service;

import com.technote.blog.entity.BlogArticle;
import com.technote.blog.model.req.RecyclePageQueryReq;
import com.technote.blog.model.resp.RecycleResp;
import com.technote.common.model.PageResp;

import java.util.List;

/**
 * 回收站服务。
 */
public interface RecycleService {

    /**
     * 记录文章删除快照。
     *
     * @param article 被删除文章
     * @param tagIds  删除前文章标签ID
     */
    void recordArticleDeletion(BlogArticle article, List<Long> tagIds);

    /**
     * 分页查询回收站记录。
     *
     * @param req 查询条件
     * @return 回收站分页结果
     */
    PageResp<RecycleResp> pageRecycle(RecyclePageQueryReq req);

    /**
     * 恢复回收站资源。
     *
     * @param id 回收站记录ID
     */
    void restoreRecycle(Long id);

    /**
     * 永久删除回收站资源。
     *
     * @param id 回收站记录ID
     */
    void deleteRecyclePermanently(Long id);
}
