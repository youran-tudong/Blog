package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友情链接实体，对应 blog_link 表。
 */
@Data
@TableName("blog_link")
public class BlogLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 网站名称。
     */
    private String siteName;

    /**
     * 网站地址。
     */
    private String siteUrl;

    /**
     * 网站图标。
     */
    private String iconUrl;

    /**
     * 网站描述。
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
