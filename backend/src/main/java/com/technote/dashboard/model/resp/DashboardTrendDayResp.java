package com.technote.dashboard.model.resp;

import lombok.Data;

import java.time.LocalDate;

/**
 * 仪表盘每日趋势响应。
 */
@Data
public class DashboardTrendDayResp {

    private LocalDate date;

    private Long viewCount;

    private Long publishCount;
}
