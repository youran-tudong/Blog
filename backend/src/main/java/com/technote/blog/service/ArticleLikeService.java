package com.technote.blog.service;

import com.technote.blog.model.resp.ArticleLikeStatusResp;

/**
 * 文章匿名点赞服务。
 */
public interface ArticleLikeService {

    /**
     * 查询匿名访客的文章点赞状态。
     *
     * @param slug       文章访问标识
     * @param visitorKey 匿名访客标识
     * @return 点赞状态与文章点赞总数
     */
    ArticleLikeStatusResp getLikeStatus(String slug, String visitorKey);

    /**
     * 点赞公开文章，重复请求不会重复增加计数。
     *
     * @param slug       文章访问标识
     * @param visitorKey 匿名访客标识
     * @return 点赞后的状态与文章点赞总数
     */
    ArticleLikeStatusResp likeArticle(String slug, String visitorKey);

    /**
     * 取消公开文章点赞，重复请求不会重复减少计数。
     *
     * @param slug       文章访问标识
     * @param visitorKey 匿名访客标识
     * @return 取消后的状态与文章点赞总数
     */
    ArticleLikeStatusResp unlikeArticle(String slug, String visitorKey);
}

