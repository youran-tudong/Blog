package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogRecycle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 回收站 Mapper。
 */
@Mapper
public interface BlogRecycleMapper extends BaseMapper<BlogRecycle> {

    /**
     * 查询指定资源是否已在回收站。
     *
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     * @return 回收站记录数
     */
    @Select("""
            SELECT COUNT(1)
            FROM blog_recycle
            WHERE resource_type = #{resourceType}
              AND resource_id = #{resourceId}
            """)
    Long countByResource(@Param("resourceType") String resourceType, @Param("resourceId") Long resourceId);
}
