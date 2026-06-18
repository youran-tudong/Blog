package com.technote.common.guard;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 公开提交防护配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "technote.public-submit-guard")
public class PublicSubmitGuardProperties {

    /**
     * 是否启用公开提交防护。
     */
    private boolean enabled = true;

    /**
     * 数学验证码配置。
     */
    private Captcha captcha = new Captcha();

    /**
     * 敏感词过滤配置。
     */
    private SensitiveWord sensitiveWord = new SensitiveWord();

    @Data
    public static class Captcha {

        /**
         * 是否启用数学验证码。
         */
        private boolean enabled = true;

        /**
         * 验证码有效期秒数。
         */
        private int ttlSeconds = 300;

        /**
         * 内存中最多保留的验证码数量。
         */
        private int maxEntries = 2000;
    }

    @Data
    public static class SensitiveWord {

        /**
         * 是否启用敏感词过滤。
         */
        private boolean enabled = true;

        /**
         * 命中时返回给前端的提示。
         */
        private String message = "提交内容包含暂不支持发布的词汇，请调整后再提交";

        /**
         * 需要拦截的词汇，适合演示环境做轻量防护。
         */
        private List<String> words = new ArrayList<>();
    }
}
