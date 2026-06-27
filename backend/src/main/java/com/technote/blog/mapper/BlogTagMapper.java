package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文章标签 Mapper。
 */
@Mapper
public interface BlogTagMapper extends BaseMapper<BlogTag> {

    /**
     * 查询标签关联的未删除文章数量。
     *
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM blog_article_tag at
            INNER JOIN blog_article a ON a.id = at.article_id AND a.deleted = 0
            WHERE at.tag_id = #{tagId}
            """)
    Long selectArticleCount(Long tagId);

    /**
     * 查询标签关联的公开已发布文章数量。
     *
     * @param tagId 标签ID
     * @return 公开文章数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM blog_article_tag at
            INNER JOIN blog_article a ON a.id = at.article_id
            WHERE at.tag_id = #{tagId}
              AND a.deleted = 0
              AND a.status = 1
              AND a.visibility = 1
            """)
    Long selectPublicArticleCount(@Param("tagId") Long tagId);
}
