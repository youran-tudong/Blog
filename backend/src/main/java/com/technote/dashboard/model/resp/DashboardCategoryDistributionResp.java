package com.technote.dashboard.model.resp;

import lombok.Data;

/**
 * 仪表盘分类内容分布响应。
 */
@Data
public class DashboardCategoryDistributionResp {

    private Long categoryId;

    private String categoryName;

    private Long articleCount;
}
