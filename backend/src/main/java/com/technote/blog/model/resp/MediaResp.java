package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 媒体文件响应。
 */
@Data
public class MediaResp {

    private Long id;

    private String originalName;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String mimeType;

    private String fileExt;

    private Integer quoteCount;

    private Long uploadBy;

    private LocalDateTime createTime;
}
