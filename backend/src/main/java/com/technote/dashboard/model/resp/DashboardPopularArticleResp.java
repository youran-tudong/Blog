package com.technote.dashboard.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仪表盘热门公开文章响应。
 */
@Data
public class DashboardPopularArticleResp {

    private Long id;

    private String title;

    private String slug;

    private Long viewCount;

    private LocalDateTime publishTime;
}
