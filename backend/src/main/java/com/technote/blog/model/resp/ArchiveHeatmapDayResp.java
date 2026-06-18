package com.technote.blog.model.resp;

import lombok.Data;

/**
 * 年度写作热力图单日数据。
 */
@Data
public class ArchiveHeatmapDayResp {

    /**
     * 日期，格式 yyyy-MM-dd。
     */
    private String date;

    /**
     * 当日发布文章数量。
     */
    private Long count;
}
