package com.technote.blog.controller;

import com.technote.blog.service.PublicFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台 RSS 与 sitemap XML 接口。
 */
@RestController
@RequiredArgsConstructor
public class PublicFeedController {

    private final PublicFeedService publicFeedService;

    @GetMapping(
            value = {"/rss.xml", "/public/rss.xml", "/public/feed/rss.xml"},
            produces = "application/rss+xml;charset=UTF-8")
    public ResponseEntity<String> rss() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/rss+xml;charset=UTF-8"))
                .body(publicFeedService.buildRssFeed());
    }

    @GetMapping(
            value = {"/sitemap.xml", "/public/sitemap.xml"},
            produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> sitemap() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml;charset=UTF-8"))
                .body(publicFeedService.buildSitemap());
    }

    @GetMapping(
            value = {"/robots.txt", "/public/robots.txt"},
            produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> robots() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .body(publicFeedService.buildRobotsTxt());
    }
}
