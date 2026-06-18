CREATE TABLE IF NOT EXISTS blog_guard_setting (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用公开提交防护：0否 1是',
  captcha_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用数学验证码：0否 1是',
  captcha_ttl_seconds INT NOT NULL DEFAULT 300 COMMENT '验证码有效期秒数',
  captcha_max_entries INT NOT NULL DEFAULT 2000 COMMENT '内存中最多保留的验证码数量',
  sensitive_word_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用敏感词过滤：0否 1是',
  sensitive_word_message VARCHAR(120) DEFAULT NULL COMMENT '敏感词命中提示',
  sensitive_words TEXT COMMENT '敏感词列表，按行存储',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='公开提交风控设置表';

INSERT INTO blog_guard_setting (
  enabled,
  captcha_enabled,
  captcha_ttl_seconds,
  captcha_max_entries,
  sensitive_word_enabled,
  sensitive_word_message,
  sensitive_words
)
SELECT 1, 1, 300, 2000, 1, '提交内容包含暂不支持发布的词汇，请调整后再提交',
       CONCAT_WS(CHAR(10), '博彩', '贷款', '发票', '代刷', 'spam')
WHERE NOT EXISTS (SELECT 1 FROM blog_guard_setting LIMIT 1);
