package com.technote.blog.model.resp;

import lombok.Data;

import java.util.List;

/**
 * 年度写作热力图响应。
 */
@Data
public class ArchiveHeatmapResp {

    /**
     * 统计年份。
     */
    private Integer year;

    /**
     * 全年公开文章发布总量。
     */
    private Long total;

    /**
     * 每日发布数量。
     */
    private List<ArchiveHeatmapDayResp> days;
}
