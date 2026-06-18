package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogArticleVersion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文章历史版本 Mapper。
 */
@Mapper
public interface BlogArticleVersionMapper extends BaseMapper<BlogArticleVersion> {

    /**
     * 查询文章当前最大版本号。
     *
     * @param articleId 文章ID
     * @return 最大版本号
     */
    @Select("SELECT COALESCE(MAX(version_no), 0) FROM blog_article_version WHERE article_id = #{articleId}")
    Integer selectMaxVersionNo(Long articleId);

    /**
     * 按文章ID物理删除历史版本。
     *
     * @param articleId 文章ID
     * @return 影响行数
     */
    @Delete("DELETE FROM blog_article_version WHERE article_id = #{articleId}")
    int hardDeleteByArticleId(@Param("articleId") Long articleId);
}
