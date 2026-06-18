package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链申请实体，对应 blog_link_apply 表。
 */
@Data
@TableName("blog_link_apply")
public class BlogLinkApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请网站名称。
     */
    private String siteName;

    /**
     * 申请网站地址。
     */
    private String siteUrl;

    /**
     * 网站图标地址。
     */
    private String iconUrl;

    /**
     * 网站描述。
     */
    private String description;

    /**
     * 申请人联系邮箱。
     */
    private String applicantEmail;

    /**
     * 状态：0待审核 1通过 2驳回。
     */
    private Integer status;

    /**
     * 审核备注。
     */
    private String auditRemark;

    private Long auditBy;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除 1已删除。
     */
    @TableLogic
    private Integer deleted;
}
