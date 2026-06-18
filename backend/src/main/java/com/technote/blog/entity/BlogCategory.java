package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章分类实体，对应 blog_category 表。
 */
@Data
@TableName("blog_category")
public class BlogCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 分类访问标识。
     */
    private String slug;

    /**
     * 分类描述。
     */
    private String description;

    /**
     * 排序值，越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 状态：0隐藏 1显示。
     */
    private Integer status;

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

