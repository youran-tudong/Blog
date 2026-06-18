package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogArticle;
import com.technote.blog.model.dto.ArticleCategoryCountStat;
import com.technote.blog.model.dto.ArticleDailyCountStat;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章 Mapper。
 */
@Mapper
public interface BlogArticleMapper extends BaseMapper<BlogArticle> {

    /**
     * 公开文章阅读量自增。
     *
     * @param id 文章ID
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_article
            SET view_count = view_count + 1,
                update_time = update_time
            WHERE id = #{id}
              AND deleted = 0
              AND status = 1
              AND visibility = 1
            """)
    int increasePublicViewCount(@Param("id") Long id);

    /**
     * 公开文章点赞数自增。
     *
     * @param id 文章ID
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_article
            SET like_count = like_count + 1,
                update_time = update_time
            WHERE id = #{id}
              AND deleted = 0
              AND status = 1
              AND visibility = 1
            """)
    int increasePublicLikeCount(@Param("id") Long id);

    /**
     * 公开文章点赞数自减，条件限制避免计数变成负数。
     *
     * @param id 文章ID
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_article
            SET like_count = like_count - 1,
                update_time = update_time
            WHERE id = #{id}
              AND deleted = 0
              AND status = 1
              AND visibility = 1
              AND like_count > 0
            """)
    int decreasePublicLikeCount(@Param("id") Long id);

    /**
     * 查询公开文章当前点赞数。
     *
     * @param id 文章ID
     * @return 当前点赞数，文章不可公开访问时返回空
     */
    @Select("""
            SELECT like_count
            FROM blog_article
            WHERE id = #{id}
              AND deleted = 0
              AND status = 1
              AND visibility = 1
            LIMIT 1
            """)
    Long selectPublicLikeCount(@Param("id") Long id);

    /**
     * 统计未删除文章总阅读量。
     *
     * @return 总阅读量
     */
    @Select("""
            SELECT COALESCE(SUM(view_count), 0)
            FROM blog_article
            WHERE deleted = 0
            """)
    Long selectTotalViewCount();

    /**
     * 按日统计指定时间范围内的文章发布量。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间，不包含该时间点
     * @return 每日发布量
     */
    @Select("""
            SELECT DATE(publish_time) AS stat_date, COUNT(*) AS count
            FROM blog_article
            WHERE deleted = 0
              AND status = 1
              AND publish_time >= #{startTime}
              AND publish_time < #{endTime}
            GROUP BY DATE(publish_time)
            ORDER BY stat_date
            """)
    List<ArticleDailyCountStat> selectDailyPublishCounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按日统计指定时间范围内的公开文章发布量。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间，不包含该时间点
     * @return 每日公开文章发布量
     */
    @Select("""
            SELECT DATE(COALESCE(publish_time, update_time, create_time)) AS stat_date,
                   COUNT(*) AS count
            FROM blog_article
            WHERE deleted = 0
              AND status = 1
              AND visibility = 1
              AND COALESCE(publish_time, update_time, create_time) >= #{startTime}
              AND COALESCE(publish_time, update_time, create_time) < #{endTime}
            GROUP BY DATE(COALESCE(publish_time, update_time, create_time))
            ORDER BY stat_date
            """)
    List<ArticleDailyCountStat> selectPublicDailyPublishCounts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计未删除文章的分类内容数量。
     *
     * @return 分类文章数量
     */
    @Select("""
            SELECT c.id AS category_id,
                   COALESCE(c.name, '未分类') AS category_name,
                   COUNT(*) AS article_count
            FROM blog_article a
            LEFT JOIN blog_category c
              ON c.id = a.category_id
             AND c.deleted = 0
            WHERE a.deleted = 0
            GROUP BY c.id, c.name
            ORDER BY article_count DESC, category_name ASC
            """)
    List<ArticleCategoryCountStat> selectCategoryArticleCounts();

    /**
     * 查询已逻辑删除文章，用于回收站恢复。
     *
     * @param id 文章ID
     * @return 已删除文章
     */
    @Select("""
            SELECT *
            FROM blog_article
            WHERE id = #{id}
              AND deleted = 1
            LIMIT 1
            """)
    BlogArticle selectDeletedById(@Param("id") Long id);

    /**
     * 恢复已逻辑删除文章。
     *
     * @param id       文章ID
     * @param updateBy 更新人
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_article
            SET deleted = 0,
                update_by = #{updateBy},
                update_time = NOW()
            WHERE id = #{id}
              AND deleted = 1
            """)
    int restoreDeletedById(@Param("id") Long id, @Param("updateBy") Long updateBy);

    /**
     * 物理删除已在回收站中的文章。
     *
     * @param id 文章ID
     * @return 影响行数
     */
    @Delete("""
            DELETE FROM blog_article
            WHERE id = #{id}
              AND deleted = 1
            """)
    int hardDeleteDeletedById(@Param("id") Long id);

    /**
     * 更新文章所属专栏，允许设置为空。
     *
     * @param id       文章ID
     * @param columnId 专栏ID
     * @param updateBy 更新人
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_article
            SET column_id = #{columnId},
                update_by = #{updateBy},
                update_time = NOW()
            WHERE id = #{id}
              AND deleted = 0
            """)
    int updateColumnId(@Param("id") Long id, @Param("columnId") Long columnId, @Param("updateBy") Long updateBy);
}
