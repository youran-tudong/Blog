package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 媒体文件 Mapper。
 */
@Mapper
public interface BlogMediaMapper extends BaseMapper<BlogMedia> {

    /**
     * 按文件访问路径调整引用次数。
     *
     * @param filePath 文件访问路径
     * @param delta    变化值
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_media
            SET quote_count = GREATEST(quote_count + #{delta}, 0)
            WHERE file_path = #{filePath}
              AND deleted = 0
            """)
    int changeQuoteCount(@Param("filePath") String filePath, @Param("delta") int delta);

    /**
     * 仅当媒体未被引用时逻辑删除。
     *
     * @param id 媒体ID
     * @return 影响行数
     */
    @Update("""
            UPDATE blog_media
            SET deleted = 1
            WHERE id = #{id}
              AND quote_count = 0
              AND deleted = 0
            """)
    int markDeletedIfUnquoted(@Param("id") Long id);
}
