package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前台公开留言响应，不包含访客邮箱和后台审核信息。
 */
@Data
public class PublicGuestbookResp {

    private Long id;

    private String nickname;

    private String content;

    private String replyContent;

    private LocalDateTime createTime;
}
