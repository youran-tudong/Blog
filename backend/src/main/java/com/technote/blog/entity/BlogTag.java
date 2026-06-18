package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章标签实体，对应 blog_tag 表。
 */
@Data
@TableName("blog_tag")
public class BlogTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称。
     */
    private String name;

    /**
     * 标签访问标识。
     */
    private String slug;

    /**
     * 标签颜色。
     */
    private String color;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除 1已删除。
     */
    @TableLogic
    private Integer deleted;
}

