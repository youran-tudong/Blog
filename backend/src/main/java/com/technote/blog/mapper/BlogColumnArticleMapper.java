package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogColumnArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 专栏文章关联 Mapper。
 */
@Mapper
public interface BlogColumnArticleMapper extends BaseMapper<BlogColumnArticle> {

    /**
     * 查询专栏内最大排序值。
     *
     * @param columnId 专栏ID
     * @return 最大排序值
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM blog_column_article WHERE column_id = #{columnId}")
    Integer selectMaxSortOrder(@Param("columnId") Long columnId);

    /**
     * 更新专栏内文章排序。
     *
     * @param columnId  专栏ID
     * @param articleId 文章ID
     * @param sortOrder 排序值
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_column_article
            SET sort_order = #{sortOrder}
            WHERE column_id = #{columnId}
              AND article_id = #{articleId}
            """)
    int updateSortOrder(@Param("columnId") Long columnId,
                        @Param("articleId") Long articleId,
                        @Param("sortOrder") Integer sortOrder);
}
