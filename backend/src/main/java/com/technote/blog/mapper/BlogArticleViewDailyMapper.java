package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogArticleViewDaily;
import com.technote.blog.model.dto.ArticleDailyCountStat;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 文章每日阅读量 Mapper。
 */
@Mapper
public interface BlogArticleViewDailyMapper extends BaseMapper<BlogArticleViewDaily> {

    /**
     * 原子累加指定文章当天阅读量。
     *
     * @param articleId 文章ID
     * @param statDate  统计日期
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO blog_article_view_daily (article_id, stat_date, view_count)
            VALUES (#{articleId}, #{statDate}, 1)
            ON DUPLICATE KEY UPDATE
              view_count = view_count + 1,
              update_time = NOW()
            """)
    int increaseDailyViewCount(@Param("articleId") Long articleId, @Param("statDate") LocalDate statDate);

    /**
     * 按文章ID物理删除每日阅读量。
     *
     * @param articleId 文章ID
     * @return 影响行数
     */
    @Delete("DELETE FROM blog_article_view_daily WHERE article_id = #{articleId}")
    int hardDeleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 按日汇总指定日期范围内的阅读量。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日阅读量
     */
    @Select("""
            SELECT stat_date, SUM(view_count) AS count
            FROM blog_article_view_daily
            WHERE stat_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY stat_date
            ORDER BY stat_date
            """)
    List<ArticleDailyCountStat> selectDailyViewCounts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
