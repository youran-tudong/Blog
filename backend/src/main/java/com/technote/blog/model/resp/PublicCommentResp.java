package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 前台公开评论响应，不包含访客邮箱和审核信息。
 */
@Data
public class PublicCommentResp {

    private Long id;

    private Long articleId;

    private Long parentId;

    private String nickname;

    private String website;

    private String content;

    private String replyContent;

    private LocalDateTime createTime;

    /**
     * 一级评论下已通过审核的回复。
     */
    private List<PublicCommentResp> replies = new ArrayList<>();
}

