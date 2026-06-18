package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章评论 Mapper。
 */
@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {

    /**
     * 按文章ID物理删除评论。
     *
     * @param articleId 文章ID
     * @return 影响行数
     */
    @Delete("DELETE FROM blog_comment WHERE article_id = #{articleId}")
    int hardDeleteByArticleId(@Param("articleId") Long articleId);
}
