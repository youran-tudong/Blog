package com.technote.blog.service;

import com.technote.blog.model.req.ArticleFlagUpdateReq;
import com.technote.blog.model.req.ArticlePageQueryReq;
import com.technote.blog.model.req.ArticleSaveReq;
import com.technote.blog.model.req.PublicArticlePageQueryReq;
import com.technote.blog.model.req.PublicArticleSearchReq;
import com.technote.blog.model.resp.ArchiveHeatmapResp;
import com.technote.blog.model.resp.ArchiveMonthResp;
import com.technote.blog.model.resp.ArticleListResp;
import com.technote.blog.model.resp.ArticleResp;
import com.technote.blog.model.resp.ArticleSearchResp;
import com.technote.blog.model.resp.ArticleVersionDetailResp;
import com.technote.blog.model.resp.ArticleVersionResp;
import com.technote.common.model.PageResp;

import java.util.List;

/**
 * 文章管理服务。
 */
public interface ArticleService {

    /**
     * 分页查询后台文章列表。
     *
     * @param req 查询条件
     * @return 文章分页结果
     */
    PageResp<ArticleListResp> pageArticles(ArticlePageQueryReq req);

    /**
     * 查询文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleResp getArticle(Long id);

    /**
     * 新增文章。
     *
     * @param req 文章保存请求
     * @return 文章详情
     */
    ArticleResp createArticle(ArticleSaveReq req);

    /**
     * 修改文章。修改前会生成历史版本，防止内容丢失。
     *
     * @param id  文章ID
     * @param req 文章保存请求
     * @return 文章详情
     */
    ArticleResp updateArticle(Long id, ArticleSaveReq req);

    /**
     * 删除文章，当前使用逻辑删除。
     *
     * @param id 文章ID
     */
    void deleteArticle(Long id);

    /**
     * 批量删除文章，当前使用逻辑删除并记录回收站快照。
     *
     * @param articleIds 文章ID列表
     */
    void batchDeleteArticles(List<Long> articleIds);

    /**
     * 批量更新文章公开/私密状态。
     *
     * @param articleIds 文章ID列表
     * @param visibility 可见性：0私密 1公开
     */
    void batchUpdateVisibility(List<Long> articleIds, Integer visibility);

    /**
     * 批量更新文章分类。
     *
     * @param articleIds  文章ID列表
     * @param categoryId 分类ID，为空时移出分类
     */
    void batchUpdateCategory(List<Long> articleIds, Long categoryId);

    /**
     * 查询文章历史版本列表。
     *
     * @param id 文章ID
     * @return 历史版本列表
     */
    List<ArticleVersionResp> listArticleVersions(Long id);

    /**
     * 查询文章历史版本详情。
     *
     * @param id        文章ID
     * @param versionId 版本ID
     * @return 历史版本详情
     */
    ArticleVersionDetailResp getArticleVersion(Long id, Long versionId);

    /**
     * 回退到指定历史版本。回退前会生成当前内容快照。
     *
     * @param id        文章ID
     * @param versionId 版本ID
     * @return 回退后的文章详情
     */
    ArticleResp rollbackArticleVersion(Long id, Long versionId);

    /**
     * 更新置顶状态。
     *
     * @param id  文章ID
     * @param req 状态请求
     */
    void updateTopFlag(Long id, ArticleFlagUpdateReq req);

    /**
     * 更新公开/私密状态。
     *
     * @param id  文章ID
     * @param req 可见性请求
     */
    void updateVisibility(Long id, ArticleFlagUpdateReq req);

    /**
     * 分页查询前台公开文章列表。
     *
     * @param req 查询条件
     * @return 公开文章分页结果
     */
    PageResp<ArticleListResp> pagePublicArticles(PublicArticlePageQueryReq req);

    /**
     * 全文搜索公开文章。
     *
     * @param req 搜索条件
     * @return 搜索结果分页
     */
    PageResp<ArticleSearchResp> searchPublicArticles(PublicArticleSearchReq req);

    /**
     * 根据访问标识查询公开文章详情，并增加阅读量。
     *
     * @param slug 文章访问标识
     * @return 公开文章详情
     */
    ArticleResp getPublicArticleBySlug(String slug);

    /**
     * 查询公开热门文章，按阅读量和发布时间排序。
     *
     * @param limit 返回数量
     * @return 热门文章列表
     */
    List<ArticleListResp> listPopularPublicArticles(Integer limit);

    /**
     * 查询指定公开文章的相关文章。
     * 优先推荐同专栏、同分类和具有共同标签的文章。
     *
     * @param slug  当前文章访问标识
     * @param limit 返回数量
     * @return 相关文章列表
     */
    List<ArticleListResp> listRelatedPublicArticles(String slug, Integer limit);

    /**
     * 查询前台公开文章归档。
     *
     * @return 按月份分组的公开文章
     */
    List<ArchiveMonthResp> listPublicArchives();

    /**
     * 查询指定年份公开文章发布热力图。
     *
     * @param year 年份
     * @return 年度写作热力图
     */
    ArchiveHeatmapResp getPublicArchiveHeatmap(Integer year);
}
