package com.technote.dashboard.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仪表盘最近文章响应。
 */
@Data
public class DashboardRecentArticleResp {

    private Long id;

    private String title;

    private String slug;

    private Integer status;

    private Integer visibility;

    private Long viewCount;

    private LocalDateTime updateTime;
}
