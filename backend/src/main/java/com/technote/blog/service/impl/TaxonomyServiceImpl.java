package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogCategory;
import com.technote.blog.entity.BlogTag;
import com.technote.blog.enums.VisibleStatusEnum;
import com.technote.blog.mapper.BlogCategoryMapper;
import com.technote.blog.mapper.BlogTagMapper;
import com.technote.blog.model.req.CategorySaveReq;
import com.technote.blog.model.req.SortUpdateReq;
import com.technote.blog.model.req.TagSaveReq;
import com.technote.blog.model.req.TaxonomyPageQueryReq;
import com.technote.blog.model.resp.CategoryResp;
import com.technote.blog.model.resp.TagResp;
import com.technote.blog.service.TaxonomyService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类与标签服务实现。
 */
@Service
@RequiredArgsConstructor
public class TaxonomyServiceImpl implements TaxonomyService {

    private final BlogCategoryMapper categoryMapper;

    private final BlogTagMapper tagMapper;

    @Override
    public PageResp<CategoryResp> pageCategories(TaxonomyPageQueryReq req) {
        IPage<BlogCategory> page = categoryMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildCategoryQuery(req));
        List<CategoryResp> records = page.getRecords().stream().map(this::toCategoryResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResp createCategory(CategorySaveReq req) {
        validateStatus(req.getStatus());
        ensureCategorySlugUnique(null, req.getSlug());
        BlogCategory category = new BlogCategory();
        fillCategory(category, req);
        category.setCreateBy(currentUserId());
        category.setUpdateBy(currentUserId());
        categoryMapper.insert(category);
        return toCategoryResp(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResp updateCategory(Long id, CategorySaveReq req) {
        validateStatus(req.getStatus());
        BlogCategory category = getCategoryOrThrow(id);
        ensureCategorySlugUnique(id, req.getSlug());
        fillCategory(category, req);
        category.setUpdateBy(currentUserId());
        categoryMapper.updateById(category);
        return toCategoryResp(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategorySort(Long id, SortUpdateReq req) {
        BlogCategory category = getCategoryOrThrow(id);
        category.setSortOrder(req.getSortOrder());
        category.setUpdateBy(currentUserId());
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        getCategoryOrThrow(id);
        Long articleCount = categoryMapper.selectArticleCount(id);
        if (articleCount != null && articleCount > 0) {
            throw new BaseException(400, "分类下存在文章，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryResp> listVisibleCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>()
                        .eq(BlogCategory::getStatus, VisibleStatusEnum.VISIBLE.getCode())
                        .orderByAsc(BlogCategory::getSortOrder)
                        .orderByDesc(BlogCategory::getCreateTime))
                .stream()
                .map(this::toCategoryResp)
                .toList();
    }

    @Override
    public PageResp<TagResp> pageTags(TaxonomyPageQueryReq req) {
        IPage<BlogTag> page = tagMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildTagQuery(req));
        List<TagResp> records = page.getRecords().stream().map(this::toTagResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagResp createTag(TagSaveReq req) {
        ensureTagSlugUnique(null, req.getSlug());
        BlogTag tag = new BlogTag();
        fillTag(tag, req);
        tag.setCreateBy(currentUserId());
        tag.setUpdateBy(currentUserId());
        tagMapper.insert(tag);
        return toTagResp(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagResp updateTag(Long id, TagSaveReq req) {
        BlogTag tag = getTagOrThrow(id);
        ensureTagSlugUnique(id, req.getSlug());
        fillTag(tag, req);
        tag.setUpdateBy(currentUserId());
        tagMapper.updateById(tag);
        return toTagResp(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        getTagOrThrow(id);
        Long articleCount = tagMapper.selectArticleCount(id);
        if (articleCount != null && articleCount > 0) {
            throw new BaseException(400, "标签已关联文章，不能删除");
        }
        tagMapper.deleteById(id);
    }

    @Override
    public List<TagResp> listTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>()
                        .orderByDesc(BlogTag::getCreateTime))
                .stream()
                .map(this::toTagResp)
                .toList();
    }

    private LambdaQueryWrapper<BlogCategory> buildCategoryQuery(TaxonomyPageQueryReq req) {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getStatus() != null, BlogCategory::getStatus, req.getStatus());
        wrapper.and(StrUtil.isNotBlank(req.getKeyword()), item -> item
                .like(BlogCategory::getName, req.getKeyword())
                .or()
                .like(BlogCategory::getSlug, req.getKeyword()));
        wrapper.orderByAsc(BlogCategory::getSortOrder).orderByDesc(BlogCategory::getCreateTime);
        return wrapper;
    }

    private LambdaQueryWrapper<BlogTag> buildTagQuery(TaxonomyPageQueryReq req) {
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(req.getKeyword()), item -> item
                .like(BlogTag::getName, req.getKeyword())
                .or()
                .like(BlogTag::getSlug, req.getKeyword()));
        wrapper.orderByDesc(BlogTag::getCreateTime);
        return wrapper;
    }

    private BlogCategory getCategoryOrThrow(Long id) {
        BlogCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BaseException(404, "分类不存在");
        }
        return category;
    }

    private BlogTag getTagOrThrow(Long id) {
        BlogTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BaseException(404, "标签不存在");
        }
        return tag;
    }

    private void validateStatus(Integer status) {
        if (!VisibleStatusEnum.contains(status)) {
            throw new BaseException(400, "状态值不正确");
        }
    }

    private void ensureCategorySlugUnique(Long id, String slug) {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<BlogCategory>()
                .eq(BlogCategory::getSlug, normalizeSlug(slug))
                .ne(id != null, BlogCategory::getId, id));
        if (count > 0) {
            throw new BaseException(400, "分类访问标识已存在");
        }
    }

    private void ensureTagSlugUnique(Long id, String slug) {
        Long count = tagMapper.selectCount(new LambdaQueryWrapper<BlogTag>()
                .eq(BlogTag::getSlug, normalizeSlug(slug))
                .ne(id != null, BlogTag::getId, id));
        if (count > 0) {
            throw new BaseException(400, "标签访问标识已存在");
        }
    }

    private void fillCategory(BlogCategory category, CategorySaveReq req) {
        category.setName(req.getName().trim());
        category.setSlug(normalizeSlug(req.getSlug()));
        category.setDescription(StrUtil.blankToDefault(req.getDescription(), null));
        category.setSortOrder(req.getSortOrder());
        category.setStatus(req.getStatus());
    }

    private void fillTag(BlogTag tag, TagSaveReq req) {
        tag.setName(req.getName().trim());
        tag.setSlug(normalizeSlug(req.getSlug()));
        tag.setColor(StrUtil.blankToDefault(req.getColor(), null));
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }

    private CategoryResp toCategoryResp(BlogCategory category) {
        CategoryResp resp = new CategoryResp();
        resp.setId(category.getId());
        resp.setName(category.getName());
        resp.setSlug(category.getSlug());
        resp.setDescription(category.getDescription());
        resp.setSortOrder(category.getSortOrder());
        resp.setStatus(category.getStatus());
        resp.setArticleCount(categoryMapper.selectArticleCount(category.getId()));
        resp.setCreateTime(category.getCreateTime());
        resp.setUpdateTime(category.getUpdateTime());
        return resp;
    }

    private TagResp toTagResp(BlogTag tag) {
        TagResp resp = new TagResp();
        resp.setId(tag.getId());
        resp.setName(tag.getName());
        resp.setSlug(tag.getSlug());
        resp.setColor(tag.getColor());
        resp.setArticleCount(tagMapper.selectArticleCount(tag.getId()));
        resp.setCreateTime(tag.getCreateTime());
        resp.setUpdateTime(tag.getUpdateTime());
        return resp;
    }
}

