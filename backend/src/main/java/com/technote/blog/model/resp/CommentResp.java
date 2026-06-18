package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章评论响应。
 */
@Data
public class CommentResp {

    private Long id;

    private Long articleId;

    private String articleTitle;

    private Long parentId;

    private String nickname;

    private String email;

    private String website;

    private String content;

    private Integer status;

    private String replyContent;

    private Long auditBy;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
