package com.technote.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.technote.blog.model.req.PublicArticlePageQueryReq;
import com.technote.blog.model.resp.ArticleListResp;
import com.technote.blog.model.resp.SettingResp;
import com.technote.blog.service.ArticleService;
import com.technote.blog.service.PublicFeedService;
import com.technote.blog.service.SettingService;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 公开订阅与站点地图服务实现。
 */
@Service
@RequiredArgsConstructor
public class PublicFeedServiceImpl implements PublicFeedService {

    private static final int RSS_ARTICLE_LIMIT = 20;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private final ArticleService articleService;

    private final SettingService settingService;

    @Value("${technote.public-site-url:http://localhost:5173}")
    private String publicSiteUrl;

    @Override
    public String buildRssFeed() {
        SettingResp setting = settingService.getSetting();
        List<ArticleListResp> articles = listLatestPublicArticles();
        String baseUrl = normalizeBaseUrl(publicSiteUrl);
        String siteTitle = StrUtil.blankToDefault(setting.getSiteTitle(), "TechNote");
        String siteDescription = StrUtil.blankToDefault(setting.getSiteDescription(), "TechNote 技术博客");

        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<rss version=\"2.0\">\n");
        builder.append("  <channel>\n");
        appendTag(builder, 4, "title", siteTitle);
        appendTag(builder, 4, "link", baseUrl + "/");
        appendTag(builder, 4, "description", siteDescription);
        appendTag(builder, 4, "language", "zh-CN");
        appendTag(builder, 4, "lastBuildDate", formatRssDate(resolveLatestTime(articles, setting.getUpdateTime())));
        for (ArticleListResp article : articles) {
            String articleUrl = buildArticleUrl(baseUrl, article.getSlug());
            builder.append("    <item>\n");
            appendTag(builder, 6, "title", article.getTitle());
            appendTag(builder, 6, "link", articleUrl);
            appendTag(builder, 6, "guid", articleUrl);
            appendTag(builder, 6, "description", StrUtil.blankToDefault(article.getSummary(), ""));
            appendTag(builder, 6, "pubDate", formatRssDate(resolveArticleTime(article)));
            builder.append("    </item>\n");
        }
        builder.append("  </channel>\n");
        builder.append("</rss>\n");
        return builder.toString();
    }

    @Override
    public String buildSitemap() {
        String baseUrl = normalizeBaseUrl(publicSiteUrl);
        List<ArticleListResp> articles = listAllPublicArticles();
        Map<String, LocalDateTime> urls = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        urls.put(baseUrl + "/", now);
        urls.put(baseUrl + "/archives", now);
        urls.put(baseUrl + "/columns", now);
        urls.put(baseUrl + "/links", now);
        urls.put(baseUrl + "/guestbook", now);
        urls.put(baseUrl + "/about", now);
        for (ArticleListResp article : articles) {
            urls.put(buildArticleUrl(baseUrl, article.getSlug()), resolveArticleTime(article));
            if (StrUtil.isNotBlank(article.getColumnSlug())) {
                urls.put(baseUrl + "/columns/" + encodePathSegment(article.getColumnSlug()), resolveArticleTime(article));
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        urls.forEach((url, lastmod) -> {
            builder.append("  <url>\n");
            appendTag(builder, 4, "loc", url);
            appendTag(builder, 4, "lastmod", formatSitemapDate(lastmod));
            builder.append("  </url>\n");
        });
        builder.append("</urlset>\n");
        return builder.toString();
    }

    @Override
    public String buildRobotsTxt() {
        String baseUrl = normalizeBaseUrl(publicSiteUrl);
        StringBuilder builder = new StringBuilder();
        builder.append("User-agent: *\n");
        builder.append("Allow: /\n");
        builder.append("Disallow: /admin\n");
        builder.append("Disallow: /admin/\n");
        builder.append("Sitemap: ").append(baseUrl).append("/sitemap.xml\n");
        return builder.toString();
    }

    private List<ArticleListResp> listLatestPublicArticles() {
        PublicArticlePageQueryReq req = new PublicArticlePageQueryReq();
        req.setPageNo(1L);
        req.setPageSize((long) RSS_ARTICLE_LIMIT);
        PageResp<ArticleListResp> page = articleService.pagePublicArticles(req);
        return page.getRecords() == null ? List.of() : page.getRecords();
    }

    private List<ArticleListResp> listAllPublicArticles() {
        List<ArticleListResp> articles = new ArrayList<>();
        long pageNo = 1L;
        long pageSize = 50L;
        while (true) {
            PublicArticlePageQueryReq req = new PublicArticlePageQueryReq();
            req.setPageNo(pageNo);
            req.setPageSize(pageSize);
            PageResp<ArticleListResp> page = articleService.pagePublicArticles(req);
            List<ArticleListResp> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                break;
            }
            articles.addAll(records);
            long total = page.getTotal() == null ? articles.size() : page.getTotal();
            if (pageNo * pageSize >= total) {
                break;
            }
            pageNo++;
        }
        return articles;
    }

    private String normalizeBaseUrl(String url) {
        String normalized = StrUtil.blankToDefault(url, "http://localhost:5173").trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String buildArticleUrl(String baseUrl, String slug) {
        return baseUrl + "/articles/" + encodePathSegment(slug);
    }

    private String encodePathSegment(String value) {
        return UriUtils.encodePathSegment(StrUtil.blankToDefault(value, ""), StandardCharsets.UTF_8);
    }

    private LocalDateTime resolveArticleTime(ArticleListResp article) {
        if (article.getPublishTime() != null) {
            return article.getPublishTime();
        }
        if (article.getUpdateTime() != null) {
            return article.getUpdateTime();
        }
        return article.getCreateTime() == null ? LocalDateTime.now() : article.getCreateTime();
    }

    private LocalDateTime resolveLatestTime(List<ArticleListResp> articles, LocalDateTime fallback) {
        return articles.stream()
                .map(this::resolveArticleTime)
                .max(Comparator.naturalOrder())
                .orElse(fallback == null ? LocalDateTime.now() : fallback);
    }

    private String formatRssDate(LocalDateTime time) {
        return toZonedDateTime(time).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private String formatSitemapDate(LocalDateTime time) {
        return toZonedDateTime(time).toLocalDate().toString();
    }

    private ZonedDateTime toZonedDateTime(LocalDateTime time) {
        return (time == null ? LocalDateTime.now() : time).atZone(DEFAULT_ZONE);
    }

    private void appendTag(StringBuilder builder, int indent, String tagName, String value) {
        builder.append(" ".repeat(indent))
                .append("<")
                .append(tagName)
                .append(">")
                .append(escapeXml(value))
                .append("</")
                .append(tagName)
                .append(">\n");
    }

    private String escapeXml(String value) {
        return StrUtil.blankToDefault(value, "")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
