package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收站实体，对应 blog_recycle 表。
 */
@Data
@TableName("blog_recycle")
public class BlogRecycle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源类型：ARTICLE/CATEGORY/TAG/COLUMN/MEDIA 等。
     */
    private String resourceType;

    /**
     * 被删除资源ID。
     */
    private Long resourceId;

    /**
     * 展示标题。
     */
    private String title;

    /**
     * 删除前 JSON 快照。
     */
    private String contentSnapshot;

    /**
     * 删除人。
     */
    private Long deleteBy;

    /**
     * 删除时间。
     */
    private LocalDateTime deleteTime;
}
