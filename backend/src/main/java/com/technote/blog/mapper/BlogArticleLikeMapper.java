package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogArticleLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文章匿名点赞关系 Mapper。
 */
@Mapper
public interface BlogArticleLikeMapper extends BaseMapper<BlogArticleLike> {

    /**
     * 幂等新增点赞关系，唯一键冲突时不重复插入。
     *
     * @param articleId  文章ID
     * @param visitorKey 匿名访客标识
     * @return 影响行数，1表示首次点赞，0表示已经点赞
     */
    @Insert("""
            INSERT IGNORE INTO blog_article_like (article_id, visitor_key)
            VALUES (#{articleId}, #{visitorKey})
            """)
    int insertIgnore(@Param("articleId") Long articleId, @Param("visitorKey") String visitorKey);

    /**
     * 查询匿名访客是否已点赞文章。
     *
     * @param articleId  文章ID
     * @param visitorKey 匿名访客标识
     * @return 点赞关系数量
     */
    @Select("""
            SELECT COUNT(*)
            FROM blog_article_like
            WHERE article_id = #{articleId}
              AND visitor_key = #{visitorKey}
            """)
    long countByArticleAndVisitor(@Param("articleId") Long articleId, @Param("visitorKey") String visitorKey);

    /**
     * 取消匿名访客对文章的点赞。
     *
     * @param articleId  文章ID
     * @param visitorKey 匿名访客标识
     * @return 影响行数
     */
    @Delete("""
            DELETE FROM blog_article_like
            WHERE article_id = #{articleId}
              AND visitor_key = #{visitorKey}
            """)
    int deleteByArticleAndVisitor(@Param("articleId") Long articleId, @Param("visitorKey") String visitorKey);

    /**
     * 按文章ID物理删除点赞关系。
     *
     * @param articleId 文章ID
     * @return 影响行数
     */
    @Delete("DELETE FROM blog_article_like WHERE article_id = #{articleId}")
    int hardDeleteByArticleId(@Param("articleId") Long articleId);
}

