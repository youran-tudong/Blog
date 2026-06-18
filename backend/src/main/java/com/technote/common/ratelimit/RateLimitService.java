package com.technote.common.ratelimit;

import com.technote.common.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 轻量内存限流服务，适用于单机演示环境的公开写接口防刷。
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final long CLEANUP_INTERVAL_SECONDS = 300L;

    private final RateLimitProperties properties;

    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();

    private final AtomicLong lastCleanupEpochSecond = new AtomicLong(0L);

    /**
     * 校验评论提交频率。
     *
     * @param request 请求对象
     */
    public void checkComment(HttpServletRequest request) {
        check("comment", properties.getComment(), request);
    }

    /**
     * 校验留言提交频率。
     *
     * @param request 请求对象
     */
    public void checkGuestbook(HttpServletRequest request) {
        check("guestbook", properties.getGuestbook(), request);
    }

    /**
     * 校验友链申请频率。
     *
     * @param request 请求对象
     */
    public void checkLinkApply(HttpServletRequest request) {
        check("link-apply", properties.getLinkApply(), request);
    }

    /**
     * 校验点赞操作频率。点赞额外合并匿名访客标识，避免同一 IP 下多个访客互相影响过大。
     *
     * @param request    请求对象
     * @param visitorKey 匿名访客标识
     */
    public void checkArticleLike(HttpServletRequest request, String visitorKey) {
        check("article-like", properties.getArticleLike(), request, visitorKey);
    }

    private void check(String ruleName, RateLimitProperties.Rule rule, HttpServletRequest request, String... extraKeys) {
        if (!properties.isEnabled() || rule == null || rule.getLimit() <= 0 || rule.getWindowSeconds() <= 0) {
            return;
        }
        long now = Instant.now().getEpochSecond();
        cleanupExpiredCounters(now);
        String key = buildKey(ruleName, request, extraKeys);
        Counter counter = counters.computeIfAbsent(key, item -> new Counter(now, 0));
        synchronized (counter) {
            if (now - counter.windowStartEpochSecond >= rule.getWindowSeconds()) {
                counter.windowStartEpochSecond = now;
                counter.count = 0;
            }
            counter.count++;
            if (counter.count > rule.getLimit()) {
                throw new BaseException(429, rule.getMessage());
            }
        }
    }

    private String buildKey(String ruleName, HttpServletRequest request, String... extraKeys) {
        String extra = Arrays.stream(extraKeys == null ? new String[0] : extraKeys)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(":"));
        String ip = resolveClientIp(request);
        return extra.isEmpty() ? ruleName + ":" + ip : ruleName + ":" + ip + ":" + extra;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return sanitizeIp(forwardedFor.split(",")[0].trim());
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return sanitizeIp(realIp.trim());
        }
        return sanitizeIp(request.getRemoteAddr());
    }

    private String sanitizeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "unknown";
        }
        return ip.length() <= 64 ? ip : ip.substring(0, 64);
    }

    private void cleanupExpiredCounters(long now) {
        long lastCleanup = lastCleanupEpochSecond.get();
        if (now - lastCleanup < CLEANUP_INTERVAL_SECONDS || !lastCleanupEpochSecond.compareAndSet(lastCleanup, now)) {
            return;
        }
        int maxWindowSeconds = Math.max(
                Math.max(properties.getComment().getWindowSeconds(), properties.getGuestbook().getWindowSeconds()),
                Math.max(properties.getLinkApply().getWindowSeconds(), properties.getArticleLike().getWindowSeconds()));
        counters.entrySet().removeIf(entry -> now - entry.getValue().windowStartEpochSecond > maxWindowSeconds * 2L);
    }

    private static class Counter {

        private long windowStartEpochSecond;

        private int count;

        private Counter(long windowStartEpochSecond, int count) {
            this.windowStartEpochSecond = windowStartEpochSecond;
            this.count = count;
        }
    }
}

