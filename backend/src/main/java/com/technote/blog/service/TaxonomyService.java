package com.technote.blog.service;

import com.technote.blog.model.req.CategorySaveReq;
import com.technote.blog.model.req.SortUpdateReq;
import com.technote.blog.model.req.TagSaveReq;
import com.technote.blog.model.req.TaxonomyPageQueryReq;
import com.technote.blog.model.resp.CategoryResp;
import com.technote.blog.model.resp.PublicCategoryResp;
import com.technote.blog.model.resp.PublicTagResp;
import com.technote.blog.model.resp.TagResp;
import com.technote.common.model.PageResp;

import java.util.List;

/**
 * 分类与标签服务。
 */
public interface TaxonomyService {

    /**
     * 分页查询后台分类列表。
     *
     * @param req 查询条件
     * @return 分类分页结果
     */
    PageResp<CategoryResp> pageCategories(TaxonomyPageQueryReq req);

    /**
     * 新增分类。
     *
     * @param req 分类保存请求
     * @return 分类信息
     */
    CategoryResp createCategory(CategorySaveReq req);

    /**
     * 修改分类。
     *
     * @param id  分类ID
     * @param req 分类保存请求
     * @return 分类信息
     */
    CategoryResp updateCategory(Long id, CategorySaveReq req);

    /**
     * 修改分类排序。
     *
     * @param id  分类ID
     * @param req 排序请求
     */
    void updateCategorySort(Long id, SortUpdateReq req);

    /**
     * 删除分类。已有文章关联时不允许删除。
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 查询前台可见分类列表。
     *
     * @return 可见分类列表
     */
    List<PublicCategoryResp> listVisibleCategories();

    /**
     * 分页查询后台标签列表。
     *
     * @param req 查询条件
     * @return 标签分页结果
     */
    PageResp<TagResp> pageTags(TaxonomyPageQueryReq req);

    /**
     * 新增标签。
     *
     * @param req 标签保存请求
     * @return 标签信息
     */
    TagResp createTag(TagSaveReq req);

    /**
     * 修改标签。
     *
     * @param id  标签ID
     * @param req 标签保存请求
     * @return 标签信息
     */
    TagResp updateTag(Long id, TagSaveReq req);

    /**
     * 删除标签。已有文章关联时不允许删除。
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);

    /**
     * 查询前台标签列表。
     *
     * @return 标签列表
     */
    List<PublicTagResp> listTags();
}
