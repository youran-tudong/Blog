package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言响应。
 */
@Data
public class GuestbookResp {

    private Long id;

    private String nickname;

    private String email;

    private String content;

    private Integer status;

    private String replyContent;

    private Long auditBy;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
