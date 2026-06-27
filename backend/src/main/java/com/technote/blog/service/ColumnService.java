package com.technote.blog.service;

import com.technote.blog.model.req.ColumnArticleSaveReq;
import com.technote.blog.model.req.ColumnPageQueryReq;
import com.technote.blog.model.req.ColumnSaveReq;
import com.technote.blog.model.req.SortUpdateReq;
import com.technote.blog.model.resp.ColumnArticleResp;
import com.technote.blog.model.resp.ColumnResp;
import com.technote.blog.model.resp.PublicColumnResp;
import com.technote.common.model.PageResp;

import java.util.List;

/**
 * 专栏服务。
 */
public interface ColumnService {

    /**
     * 分页查询后台专栏。
     *
     * @param req 查询条件
     * @return 专栏分页结果
     */
    PageResp<ColumnResp> pageColumns(ColumnPageQueryReq req);

    /**
     * 新增专栏。
     *
     * @param req 专栏保存请求
     * @return 专栏信息
     */
    ColumnResp createColumn(ColumnSaveReq req);

    /**
     * 修改专栏。
     *
     * @param id  专栏ID
     * @param req 专栏保存请求
     * @return 专栏信息
     */
    ColumnResp updateColumn(Long id, ColumnSaveReq req);

    /**
     * 修改专栏排序。
     *
     * @param id  专栏ID
     * @param req 排序请求
     */
    void updateColumnSort(Long id, SortUpdateReq req);

    /**
     * 删除专栏。已有文章关联时不允许删除。
     *
     * @param id 专栏ID
     */
    void deleteColumn(Long id);

    /**
     * 查询专栏内文章。
     *
     * @param columnId 专栏ID
     * @return 专栏文章列表
     */
    List<ColumnArticleResp> listColumnArticles(Long columnId);

    /**
     * 添加文章到专栏。
     *
     * @param columnId 专栏ID
     * @param req      关联请求
     */
    void addArticleToColumn(Long columnId, ColumnArticleSaveReq req);

    /**
     * 修改专栏内文章排序。
     *
     * @param columnId  专栏ID
     * @param articleId 文章ID
     * @param req       排序请求
     */
    void updateColumnArticleSort(Long columnId, Long articleId, SortUpdateReq req);

    /**
     * 从专栏移除文章。
     *
     * @param columnId  专栏ID
     * @param articleId 文章ID
     */
    void removeArticleFromColumn(Long columnId, Long articleId);

    /**
     * 查询前台可见专栏。
     *
     * @return 可见专栏列表
     */
    List<PublicColumnResp> listVisibleColumns();

    /**
     * 根据访问标识查询前台可见专栏。
     *
     * @param slug 访问标识
     * @return 专栏信息
     */
    PublicColumnResp getPublicColumnBySlug(String slug);
}
