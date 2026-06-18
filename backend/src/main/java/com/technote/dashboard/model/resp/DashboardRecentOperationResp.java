package com.technote.dashboard.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仪表盘最近操作日志响应。
 */
@Data
public class DashboardRecentOperationResp {

    private Long id;

    private Long userId;

    private String module;

    private String operation;

    private Integer successFlag;

    private LocalDateTime createTime;
}
