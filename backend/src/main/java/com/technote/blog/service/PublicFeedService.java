package com.technote.blog.service;

/**
 * 公开订阅与站点地图服务。
 */
public interface PublicFeedService {

    /**
     * 生成 RSS 2.0 订阅 XML。
     *
     * @return RSS XML 字符串
     */
    String buildRssFeed();

    /**
     * 生成 sitemap.xml。
     *
     * @return sitemap XML 字符串
     */
    String buildSitemap();

    /**
     * 生成 robots.txt。
     *
     * @return robots.txt 文本
     */
    String buildRobotsTxt();
}
