package com.technote.common.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 公开互动限流配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "technote.rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流。
     */
    private boolean enabled = true;

    /**
     * 评论提交限流。
     */
    private Rule comment = new Rule(5, 60, "评论提交过于频繁，请稍后再试");

    /**
     * 留言提交限流。
     */
    private Rule guestbook = new Rule(5, 60, "留言提交过于频繁，请稍后再试");

    /**
     * 友链申请限流。
     */
    private Rule linkApply = new Rule(3, 3600, "友链申请过于频繁，请稍后再试");

    /**
     * 文章点赞/取消点赞限流。
     */
    private Rule articleLike = new Rule(60, 60, "点赞操作过于频繁，请稍后再试");

    @Data
    public static class Rule {

        /**
         * 窗口内允许的最大请求次数。
         */
        private int limit;

        /**
         * 固定窗口秒数。
         */
        private int windowSeconds;

        /**
         * 超限提示。
         */
        private String message;

        public Rule() {
        }

        public Rule(int limit, int windowSeconds, String message) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
            this.message = message;
        }
    }
}

