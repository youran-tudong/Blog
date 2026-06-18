package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 文章匿名点赞状态响应。
 */
@Data
public class ArticleLikeStatusResp {

    /**
     * 当前匿名访客是否已点赞。
     */
    private boolean liked;

    /**
     * 文章当前点赞总数。
     */
    private Long likeCount;
}

