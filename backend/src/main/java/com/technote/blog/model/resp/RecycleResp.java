package com.technote.blog.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收站记录响应。
 */
@Data
public class RecycleResp {

    private Long id;

    private String resourceType;

    private Long resourceId;

    private String title;

    private Long deleteBy;

    private LocalDateTime deleteTime;
}
