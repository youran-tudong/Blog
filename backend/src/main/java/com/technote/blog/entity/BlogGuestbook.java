package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言实体，对应 blog_guestbook 表。
 */
@Data
@TableName("blog_guestbook")
public class BlogGuestbook {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客昵称。
     */
    private String nickname;

    /**
     * 访客邮箱。
     */
    private String email;

    /**
     * 留言内容。
     */
    private String content;

    /**
     * 状态：0待审核 1通过 2驳回。
     */
    private Integer status;

    /**
     * 管理员回复。
     */
    private String replyContent;

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
