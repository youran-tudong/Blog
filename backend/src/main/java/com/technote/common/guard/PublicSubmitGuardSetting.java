package com.technote.common.guard;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开提交风控设置实体，对应 blog_guard_setting 表。
 */
@Data
@TableName("blog_guard_setting")
public class PublicSubmitGuardSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 是否启用公开提交防护：0否 1是。
     */
    private Integer enabled;

    /**
     * 是否启用数学验证码：0否 1是。
     */
    private Integer captchaEnabled;

    /**
     * 验证码有效期秒数。
     */
    private Integer captchaTtlSeconds;

    /**
     * 内存中最多保留的验证码数量。
     */
    private Integer captchaMaxEntries;

    /**
     * 是否启用敏感词过滤：0否 1是。
     */
    private Integer sensitiveWordEnabled;

    /**
     * 敏感词命中提示。
     */
    private String sensitiveWordMessage;

    /**
     * 敏感词列表，按行存储。
     */
    private String sensitiveWords;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
