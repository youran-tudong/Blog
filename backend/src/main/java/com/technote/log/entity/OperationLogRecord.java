package com.technote.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体，对应 blog_operation_log 表。
 */
@Data
@TableName("blog_operation_log")
public class OperationLogRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID。
     */
    private Long userId;

    /**
     * 操作模块。
     */
    private String module;

    /**
     * 操作内容。
     */
    private String operation;

    /**
     * 请求方法。
     */
    private String requestMethod;

    /**
     * 请求地址。
     */
    private String requestUri;

    /**
     * 请求参数。
     */
    private String requestParams;

    /**
     * 操作IP。
     */
    private String ip;

    /**
     * 浏览器标识。
     */
    private String userAgent;

    /**
     * 是否成功：0失败 1成功。
     */
    private Integer successFlag;

    /**
     * 失败原因。
     */
    private String errorMessage;

    private LocalDateTime createTime;
}
