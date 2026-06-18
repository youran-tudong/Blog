package com.technote.blog.model.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 文章按日数量统计。
 */
@Data
public class ArticleDailyCountStat {

    private LocalDate statDate;

    private Long count;
}
