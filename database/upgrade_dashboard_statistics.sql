-- 已有 TechNote 数据库升级：增加文章每日阅读量聚合表。
-- 新建数据库直接执行 technote_schema.sql，无需再执行本文件。
-- 历史累计 view_count 无法可靠拆分到每日，本表从升级完成后的新访问开始记录趋势。

CREATE TABLE blog_article_view_daily (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  view_count BIGINT NOT NULL DEFAULT 0 COMMENT '当日阅读量',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_article_view_daily (article_id, stat_date),
  KEY idx_article_view_daily_date (stat_date)
) ENGINE=InnoDB COMMENT='文章每日阅读量聚合表';
