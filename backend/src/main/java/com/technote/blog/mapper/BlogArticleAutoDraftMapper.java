package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogArticleAutoDraft;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 文章自动草稿 Mapper。
 */
@Mapper
public interface BlogArticleAutoDraftMapper extends BaseMapper<BlogArticleAutoDraft> {

    /**
     * 按草稿键和创建人物理删除自动草稿。
     * 自动草稿是临时编辑数据，正式保存后直接清理，避免逻辑删除记录影响唯一键复用。
     *
     * @param draftKey 草稿键
     * @param createBy 创建人
     * @return 影响行数
     */
    @Delete("""
            DELETE FROM blog_article_auto_draft
            WHERE draft_key = #{draftKey}
              AND create_by = #{createBy}
            """)
    int hardDeleteByDraftKeyAndCreateBy(@Param("draftKey") String draftKey, @Param("createBy") Long createBy);

    /**
     * 按主键物理删除自动草稿，用于后台维护页主动清理。
     *
     * @param id 草稿ID
     * @return 影响行数
     */
    @Delete("""
            DELETE FROM blog_article_auto_draft
            WHERE id = #{id}
            """)
    int hardDeleteById(@Param("id") Long id);

    /**
     * 物理删除过期自动草稿。默认只清理未关联正式文章的新文章草稿，降低误删编辑中草稿的风险。
     *
     * @param cutoffTime 截止更新时间
     * @param onlyOrphan 是否只清理孤立草稿
     * @return 影响行数
     */
    @Delete("""
            <script>
            DELETE FROM blog_article_auto_draft
            WHERE update_time &lt; #{cutoffTime}
            <if test="onlyOrphan != null and onlyOrphan">
              AND article_id IS NULL
            </if>
            </script>
            """)
    int hardDeleteExpired(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("onlyOrphan") Boolean onlyOrphan);
}
