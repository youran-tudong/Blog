package com.technote.log.model.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志响应。
 */
@Data
public class OperationLogResp {

    private Long id;

    private Long userId;

    private String module;

    private String operation;

    private String requestMethod;

    private String requestUri;

    private String requestParams;

    private String ip;

    private String userAgent;

    private Integer successFlag;

    private String errorMessage;

    private LocalDateTime createTime;
}
