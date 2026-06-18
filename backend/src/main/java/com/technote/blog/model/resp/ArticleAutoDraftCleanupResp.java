package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自动草稿清理结果。
 */
@Data
public class ArticleAutoDraftCleanupResp {

    /**
     * 本次物理删除的草稿数量。
     */
    private Integer deletedCount;

    /**
     * 保留天数。
     */
    private Integer retentionDays;

    /**
     * 是否只清理孤立草稿。
     */
    private Boolean onlyOrphan;

    /**
     * 早于该时间的草稿会被清理。
     */
    private LocalDateTime cutoffTime;
}
