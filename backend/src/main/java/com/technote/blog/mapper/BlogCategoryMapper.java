package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 文章分类 Mapper。
 */
@Mapper
public interface BlogCategoryMapper extends BaseMapper<BlogCategory> {

    /**
     * 查询分类下未删除文章数量。
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    @Select("SELECT COUNT(1) FROM blog_article WHERE category_id = #{categoryId} AND deleted = 0")
    Long selectArticleCount(Long categoryId);
}

