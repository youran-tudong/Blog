package com.technote.common.guard;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.technote.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 公开提交防护服务，负责后台配置、一次性数学验证码和轻量敏感词过滤。
 */
@Service
@RequiredArgsConstructor
public class PublicSubmitGuardService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String DEFAULT_SENSITIVE_WORD_MESSAGE = "提交内容包含暂不支持发布的词汇，请调整后再提交";

    private final PublicSubmitGuardProperties properties;

    private final PublicSubmitGuardSettingMapper settingMapper;

    private final ConcurrentMap<String, CaptchaRecord> captchas = new ConcurrentHashMap<>();

    /**
     * 查询后台公开提交风控设置。
     *
     * @return 当前风控设置，数据库未配置时返回 application.yml 默认值
     */
    public PublicSubmitGuardSettingResp getSetting() {
        PublicSubmitGuardSetting setting = getFirstSetting();
        return toResp(setting == null ? buildDefaultSetting() : setting);
    }

    /**
     * 保存后台公开提交风控设置。
     *
     * @param req 风控设置
     * @return 保存后的风控设置
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicSubmitGuardSettingResp saveSetting(PublicSubmitGuardSettingReq req) {
        PublicSubmitGuardSetting setting = getFirstSetting();
        if (setting == null) {
            setting = new PublicSubmitGuardSetting();
            fillSetting(setting, req);
            settingMapper.insert(setting);
            return toResp(setting);
        }
        fillSetting(setting, req);
        settingMapper.updateById(setting);
        return toResp(setting);
    }

    /**
     * 创建公开提交验证码。
     *
     * @return 验证码题目和ID
     */
    public CaptchaResp createCaptcha() {
        GuardRuntimeSetting setting = getRuntimeSetting();
        if (!setting.enabled() || !setting.captchaEnabled()) {
            CaptchaResp resp = new CaptchaResp();
            resp.setCaptchaId("");
            resp.setQuestion("验证码未启用");
            resp.setExpireSeconds(0);
            return resp;
        }
        long now = Instant.now().getEpochSecond();
        cleanupExpiredCaptchas(now);
        int maxEntries = Math.max(setting.captchaMaxEntries(), 100);
        if (captchas.size() >= maxEntries) {
            throw new BaseException(429, "验证码请求过多，请稍后再试");
        }
        CaptchaQuestion question = nextQuestion();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        long expireAt = now + Math.max(setting.captchaTtlSeconds(), 60);
        captchas.put(captchaId, new CaptchaRecord(question.answer(), expireAt));

        CaptchaResp resp = new CaptchaResp();
        resp.setCaptchaId(captchaId);
        resp.setQuestion(question.text());
        resp.setExpireSeconds((int) (expireAt - now));
        return resp;
    }

    /**
     * 校验评论公开提交。
     *
     * @param captchaId     验证码ID
     * @param captchaAnswer 验证码答案
     * @param values        需要做敏感词检查的文本
     */
    public void checkComment(String captchaId, String captchaAnswer, String... values) {
        check(captchaId, captchaAnswer, values);
    }

    /**
     * 校验留言公开提交。
     *
     * @param captchaId     验证码ID
     * @param captchaAnswer 验证码答案
     * @param values        需要做敏感词检查的文本
     */
    public void checkGuestbook(String captchaId, String captchaAnswer, String... values) {
        check(captchaId, captchaAnswer, values);
    }

    /**
     * 校验友链申请公开提交。
     *
     * @param captchaId     验证码ID
     * @param captchaAnswer 验证码答案
     * @param values        需要做敏感词检查的文本
     */
    public void checkLinkApply(String captchaId, String captchaAnswer, String... values) {
        check(captchaId, captchaAnswer, values);
    }

    private void check(String captchaId, String captchaAnswer, String... values) {
        GuardRuntimeSetting setting = getRuntimeSetting();
        if (!setting.enabled()) {
            return;
        }
        verifyCaptcha(setting, captchaId, captchaAnswer);
        ensureCleanContent(setting, values);
    }

    private PublicSubmitGuardSetting getFirstSetting() {
        return settingMapper.selectOne(new LambdaQueryWrapper<PublicSubmitGuardSetting>()
                .orderByAsc(PublicSubmitGuardSetting::getId)
                .last("LIMIT 1"));
    }

    private PublicSubmitGuardSetting buildDefaultSetting() {
        PublicSubmitGuardSetting setting = new PublicSubmitGuardSetting();
        setting.setEnabled(toInt(properties.isEnabled()));
        setting.setCaptchaEnabled(toInt(properties.getCaptcha().isEnabled()));
        setting.setCaptchaTtlSeconds(properties.getCaptcha().getTtlSeconds());
        setting.setCaptchaMaxEntries(properties.getCaptcha().getMaxEntries());
        setting.setSensitiveWordEnabled(toInt(properties.getSensitiveWord().isEnabled()));
        setting.setSensitiveWordMessage(properties.getSensitiveWord().getMessage());
        setting.setSensitiveWords(String.join("\n", properties.getSensitiveWord().getWords()));
        return setting;
    }

    private void fillSetting(PublicSubmitGuardSetting setting, PublicSubmitGuardSettingReq req) {
        setting.setEnabled(toInt(req.getEnabled()));
        setting.setCaptchaEnabled(toInt(req.getCaptchaEnabled()));
        setting.setCaptchaTtlSeconds(req.getCaptchaTtlSeconds());
        setting.setCaptchaMaxEntries(req.getCaptchaMaxEntries());
        setting.setSensitiveWordEnabled(toInt(req.getSensitiveWordEnabled()));
        setting.setSensitiveWordMessage(StrUtil.blankToDefault(req.getSensitiveWordMessage(), DEFAULT_SENSITIVE_WORD_MESSAGE));
        setting.setSensitiveWords(normalizeSensitiveWords(req.getSensitiveWords()));
    }

    private PublicSubmitGuardSettingResp toResp(PublicSubmitGuardSetting setting) {
        PublicSubmitGuardSettingResp resp = new PublicSubmitGuardSettingResp();
        resp.setId(setting.getId());
        resp.setEnabled(toBool(setting.getEnabled()));
        resp.setCaptchaEnabled(toBool(setting.getCaptchaEnabled()));
        resp.setCaptchaTtlSeconds(setting.getCaptchaTtlSeconds());
        resp.setCaptchaMaxEntries(setting.getCaptchaMaxEntries());
        resp.setSensitiveWordEnabled(toBool(setting.getSensitiveWordEnabled()));
        resp.setSensitiveWordMessage(setting.getSensitiveWordMessage());
        resp.setSensitiveWords(setting.getSensitiveWords());
        resp.setCreateTime(setting.getCreateTime());
        resp.setUpdateTime(setting.getUpdateTime());
        return resp;
    }

    private GuardRuntimeSetting getRuntimeSetting() {
        PublicSubmitGuardSetting setting = getFirstSetting();
        if (setting == null) {
            setting = buildDefaultSetting();
        }
        return new GuardRuntimeSetting(
                toBool(setting.getEnabled()),
                toBool(setting.getCaptchaEnabled()),
                Math.max(setting.getCaptchaTtlSeconds() == null ? 300 : setting.getCaptchaTtlSeconds(), 60),
                Math.max(setting.getCaptchaMaxEntries() == null ? 2000 : setting.getCaptchaMaxEntries(), 100),
                toBool(setting.getSensitiveWordEnabled()),
                StrUtil.blankToDefault(setting.getSensitiveWordMessage(), DEFAULT_SENSITIVE_WORD_MESSAGE),
                parseSensitiveWords(setting.getSensitiveWords()));
    }

    private void verifyCaptcha(GuardRuntimeSetting setting, String captchaId, String captchaAnswer) {
        if (!setting.captchaEnabled()) {
            return;
        }
        if (StrUtil.isBlank(captchaId) || StrUtil.isBlank(captchaAnswer)) {
            throw new BaseException(400, "请先完成验证码");
        }
        long now = Instant.now().getEpochSecond();
        CaptchaRecord record = captchas.remove(captchaId.trim());
        if (record == null || record.expireAtEpochSecond() < now) {
            throw new BaseException(400, "验证码已过期，请刷新后再试");
        }
        if (!record.answer().equals(captchaAnswer.trim())) {
            throw new BaseException(400, "验证码答案不正确");
        }
    }

    private void ensureCleanContent(GuardRuntimeSetting setting, String... values) {
        if (!setting.sensitiveWordEnabled() || setting.sensitiveWords().isEmpty()) {
            return;
        }
        boolean hit = Arrays.stream(values == null ? new String[0] : values)
                .filter(StrUtil::isNotBlank)
                .map(item -> item.toLowerCase(Locale.ROOT))
                .anyMatch(value -> setting.sensitiveWords().stream().anyMatch(value::contains));
        if (hit) {
            throw new BaseException(400, setting.sensitiveWordMessage());
        }
    }

    private CaptchaQuestion nextQuestion() {
        int left = RANDOM.nextInt(9) + 1;
        int right = RANDOM.nextInt(9) + 1;
        int operator = RANDOM.nextInt(3);
        if (operator == 0) {
            return new CaptchaQuestion(left + " + " + right + " = ?", String.valueOf(left + right));
        }
        if (operator == 1) {
            int max = Math.max(left, right);
            int min = Math.min(left, right);
            return new CaptchaQuestion(max + " - " + min + " = ?", String.valueOf(max - min));
        }
        return new CaptchaQuestion(left + " x " + right + " = ?", String.valueOf(left * right));
    }

    private void cleanupExpiredCaptchas(long now) {
        captchas.entrySet().removeIf(entry -> entry.getValue().expireAtEpochSecond() < now);
    }

    private String normalizeSensitiveWords(String sensitiveWords) {
        if (StrUtil.isBlank(sensitiveWords)) {
            return "";
        }
        return String.join("\n", Arrays.stream(sensitiveWords.split("\\R"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll));
    }

    private List<String> parseSensitiveWords(String sensitiveWords) {
        String normalizedWords = normalizeSensitiveWords(sensitiveWords);
        if (StrUtil.isBlank(normalizedWords)) {
            return List.of();
        }
        return Arrays.stream(normalizedWords.split("\\R"))
                .map(item -> item.toLowerCase(Locale.ROOT))
                .toList();
    }

    private int toInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private boolean toBool(Integer value) {
        return value != null && value == 1;
    }

    private record CaptchaQuestion(String text, String answer) {
    }

    private record CaptchaRecord(String answer, long expireAtEpochSecond) {
    }

    private record GuardRuntimeSetting(
            boolean enabled,
            boolean captchaEnabled,
            int captchaTtlSeconds,
            int captchaMaxEntries,
            boolean sensitiveWordEnabled,
            String sensitiveWordMessage,
            List<String> sensitiveWords) {
    }
}
