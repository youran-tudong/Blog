package com.technote.dashboard.service;

import com.technote.dashboard.model.resp.DashboardStatsResp;

/**
 * 后台仪表盘统计服务。
 */
public interface DashboardService {

    /**
     * 查询后台仪表盘统计数据。
     *
     * @return 仪表盘统计数据
     */
    DashboardStatsResp getStats();
}
