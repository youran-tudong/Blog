package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 专栏 Mapper。
 */
@Mapper
public interface BlogColumnMapper extends BaseMapper<BlogColumn> {

    /**
     * 查询专栏下未删除文章数量。
     *
     * @param columnId 专栏ID
     * @return 文章数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM blog_article
            WHERE column_id = #{columnId}
              AND deleted = 0
            """)
    Long selectArticleCount(@Param("columnId") Long columnId);

    /**
     * 查询专栏下公开文章数量。
     *
     * @param columnId 专栏ID
     * @return 公开文章数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM blog_article
            WHERE column_id = #{columnId}
              AND deleted = 0
              AND status = 1
              AND visibility = 1
            """)
    Long selectPublicArticleCount(@Param("columnId") Long columnId);
}
