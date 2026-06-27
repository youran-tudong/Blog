package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.entity.BlogColumn;
import com.technote.blog.entity.BlogColumnArticle;
import com.technote.blog.enums.VisibleStatusEnum;
import com.technote.blog.mapper.BlogArticleMapper;
import com.technote.blog.mapper.BlogColumnArticleMapper;
import com.technote.blog.mapper.BlogColumnMapper;
import com.technote.blog.model.req.ColumnArticleSaveReq;
import com.technote.blog.model.req.ColumnPageQueryReq;
import com.technote.blog.model.req.ColumnSaveReq;
import com.technote.blog.model.req.SortUpdateReq;
import com.technote.blog.model.resp.ColumnArticleResp;
import com.technote.blog.model.resp.ColumnResp;
import com.technote.blog.model.resp.PublicColumnResp;
import com.technote.blog.service.ColumnService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 专栏服务实现。
 */
@Service
@RequiredArgsConstructor
public class ColumnServiceImpl implements ColumnService {

    private final BlogColumnMapper columnMapper;

    private final BlogColumnArticleMapper columnArticleMapper;

    private final BlogArticleMapper articleMapper;

    @Override
    public PageResp<ColumnResp> pageColumns(ColumnPageQueryReq req) {
        IPage<BlogColumn> page = columnMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                buildColumnQuery(req));
        List<ColumnResp> records = page.getRecords().stream()
                .map(this::toColumnResp)
                .toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ColumnResp createColumn(ColumnSaveReq req) {
        validateStatus(req.getStatus());
        ensureSlugUnique(null, req.getSlug());
        BlogColumn column = new BlogColumn();
        fillColumn(column, req);
        column.setCreateBy(currentUserId());
        column.setUpdateBy(currentUserId());
        columnMapper.insert(column);
        return toColumnResp(column);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ColumnResp updateColumn(Long id, ColumnSaveReq req) {
        validateStatus(req.getStatus());
        BlogColumn column = getColumnOrThrow(id);
        ensureSlugUnique(id, req.getSlug());
        fillColumn(column, req);
        column.setUpdateBy(currentUserId());
        columnMapper.updateById(column);
        return toColumnResp(column);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateColumnSort(Long id, SortUpdateReq req) {
        BlogColumn column = getColumnOrThrow(id);
        column.setSortOrder(req.getSortOrder());
        column.setUpdateBy(currentUserId());
        columnMapper.updateById(column);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteColumn(Long id) {
        getColumnOrThrow(id);
        Long articleCount = columnMapper.selectArticleCount(id);
        if (articleCount != null && articleCount > 0) {
            throw new BaseException(400, "专栏下存在文章，不能删除");
        }
        columnArticleMapper.delete(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getColumnId, id));
        columnMapper.deleteById(id);
    }

    @Override
    public List<ColumnArticleResp> listColumnArticles(Long columnId) {
        getColumnOrThrow(columnId);
        List<BlogColumnArticle> relations = columnArticleMapper.selectList(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getColumnId, columnId)
                .orderByAsc(BlogColumnArticle::getSortOrder)
                .orderByDesc(BlogColumnArticle::getCreateTime));
        if (CollUtil.isEmpty(relations)) {
            return List.of();
        }
        List<Long> articleIds = relations.stream().map(BlogColumnArticle::getArticleId).toList();
        Map<Long, BlogArticle> articleMap = articleMapper.selectBatchIds(articleIds).stream()
                .collect(Collectors.toMap(BlogArticle::getId, Function.identity()));
        return relations.stream()
                .map(relation -> toColumnArticleResp(relation, articleMap.get(relation.getArticleId())))
                .filter(resp -> resp.getArticleId() != null)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArticleToColumn(Long columnId, ColumnArticleSaveReq req) {
        getColumnOrThrow(columnId);
        BlogArticle article = getArticleOrThrow(req.getArticleId());
        columnArticleMapper.delete(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getArticleId, article.getId()));
        BlogColumnArticle relation = new BlogColumnArticle();
        relation.setColumnId(columnId);
        relation.setArticleId(article.getId());
        relation.setSortOrder(req.getSortOrder() == null
                ? columnArticleMapper.selectMaxSortOrder(columnId) + 1
                : req.getSortOrder());
        columnArticleMapper.insert(relation);
        articleMapper.updateColumnId(article.getId(), columnId, currentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateColumnArticleSort(Long columnId, Long articleId, SortUpdateReq req) {
        getColumnOrThrow(columnId);
        getArticleOrThrow(articleId);
        int updated = columnArticleMapper.updateSortOrder(columnId, articleId, req.getSortOrder());
        if (updated == 0) {
            throw new BaseException(404, "专栏文章关联不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArticleFromColumn(Long columnId, Long articleId) {
        getColumnOrThrow(columnId);
        BlogArticle article = getArticleOrThrow(articleId);
        int deleted = columnArticleMapper.delete(new LambdaQueryWrapper<BlogColumnArticle>()
                .eq(BlogColumnArticle::getColumnId, columnId)
                .eq(BlogColumnArticle::getArticleId, articleId));
        if (deleted == 0) {
            throw new BaseException(404, "专栏文章关联不存在");
        }
        if (columnId.equals(article.getColumnId())) {
            articleMapper.updateColumnId(articleId, null, currentUserId());
        }
    }

    @Override
    public List<PublicColumnResp> listVisibleColumns() {
        return columnMapper.selectList(new LambdaQueryWrapper<BlogColumn>()
                        .eq(BlogColumn::getStatus, VisibleStatusEnum.VISIBLE.getCode())
                        .orderByAsc(BlogColumn::getSortOrder)
                        .orderByDesc(BlogColumn::getCreateTime))
                .stream()
                .map(this::toPublicColumnResp)
                .toList();
    }

    @Override
    public PublicColumnResp getPublicColumnBySlug(String slug) {
        BlogColumn column = columnMapper.selectOne(new LambdaQueryWrapper<BlogColumn>()
                .eq(BlogColumn::getSlug, normalizeSlug(slug))
                .eq(BlogColumn::getStatus, VisibleStatusEnum.VISIBLE.getCode())
                .last("LIMIT 1"));
        if (column == null) {
            throw new BaseException(404, "专栏不存在或未公开");
        }
        return toPublicColumnResp(column);
    }

    private LambdaQueryWrapper<BlogColumn> buildColumnQuery(ColumnPageQueryReq req) {
        LambdaQueryWrapper<BlogColumn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getStatus() != null, BlogColumn::getStatus, req.getStatus());
        wrapper.and(StrUtil.isNotBlank(req.getKeyword()), item -> item
                .like(BlogColumn::getName, req.getKeyword())
                .or()
                .like(BlogColumn::getSlug, req.getKeyword()));
        wrapper.orderByAsc(BlogColumn::getSortOrder).orderByDesc(BlogColumn::getCreateTime);
        return wrapper;
    }

    private BlogColumn getColumnOrThrow(Long id) {
        BlogColumn column = columnMapper.selectById(id);
        if (column == null) {
            throw new BaseException(404, "专栏不存在");
        }
        return column;
    }

    private BlogArticle getArticleOrThrow(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BaseException(404, "文章不存在");
        }
        return article;
    }

    private void validateStatus(Integer status) {
        if (!VisibleStatusEnum.contains(status)) {
            throw new BaseException(400, "状态值不正确");
        }
    }

    private void ensureSlugUnique(Long id, String slug) {
        Long count = columnMapper.selectCount(new LambdaQueryWrapper<BlogColumn>()
                .eq(BlogColumn::getSlug, normalizeSlug(slug))
                .ne(id != null, BlogColumn::getId, id));
        if (count > 0) {
            throw new BaseException(400, "专栏访问标识已存在");
        }
    }

    private void fillColumn(BlogColumn column, ColumnSaveReq req) {
        column.setName(req.getName().trim());
        column.setSlug(normalizeSlug(req.getSlug()));
        column.setDescription(StrUtil.blankToDefault(req.getDescription(), null));
        column.setCoverUrl(StrUtil.blankToDefault(req.getCoverUrl(), null));
        column.setSortOrder(req.getSortOrder());
        column.setStatus(req.getStatus());
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    private ColumnResp toColumnResp(BlogColumn column) {
        ColumnResp resp = new ColumnResp();
        resp.setId(column.getId());
        resp.setName(column.getName());
        resp.setSlug(column.getSlug());
        resp.setDescription(column.getDescription());
        resp.setCoverUrl(column.getCoverUrl());
        resp.setSortOrder(column.getSortOrder());
        resp.setStatus(column.getStatus());
        resp.setArticleCount(columnMapper.selectArticleCount(column.getId()));
        resp.setCreateTime(column.getCreateTime());
        resp.setUpdateTime(column.getUpdateTime());
        return resp;
    }

    private PublicColumnResp toPublicColumnResp(BlogColumn column) {
        PublicColumnResp resp = new PublicColumnResp();
        resp.setId(column.getId());
        resp.setName(column.getName());
        resp.setSlug(column.getSlug());
        resp.setDescription(column.getDescription());
        resp.setCoverUrl(column.getCoverUrl());
        resp.setArticleCount(columnMapper.selectPublicArticleCount(column.getId()));
        return resp;
    }

    private ColumnArticleResp toColumnArticleResp(BlogColumnArticle relation, BlogArticle article) {
        ColumnArticleResp resp = new ColumnArticleResp();
        if (article == null) {
            return resp;
        }
        resp.setArticleId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSlug(article.getSlug());
        resp.setStatus(article.getStatus());
        resp.setVisibility(article.getVisibility());
        resp.setSortOrder(relation.getSortOrder());
        resp.setPublishTime(article.getPublishTime());
        resp.setUpdateTime(article.getUpdateTime());
        return resp;
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
