-- 已有 TechNote 数据库升级：补齐友链申请审核字段，并增加并发防重约束。
-- 新建数据库直接执行 technote_schema.sql，无需再执行本文件。
-- 执行前请先确认 blog_link 中没有重复未删除网址，blog_link_apply 中没有重复待审核网址。

ALTER TABLE blog_link
  ADD COLUMN active_site_url VARCHAR(255)
    GENERATED ALWAYS AS (CASE WHEN deleted = 0 THEN site_url ELSE NULL END) STORED
    COMMENT '未删除友链网址，用于并发防重' AFTER deleted,
  ADD UNIQUE INDEX uk_link_active_site_url (active_site_url);

ALTER TABLE blog_link_apply
  ADD COLUMN audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注' AFTER status,
  ADD COLUMN pending_site_url VARCHAR(255)
    GENERATED ALWAYS AS (CASE WHEN status = 0 AND deleted = 0 THEN site_url ELSE NULL END) STORED
    COMMENT '待审核申请网址，用于并发防重' AFTER deleted,
  ADD UNIQUE INDEX uk_link_apply_pending_site_url (pending_site_url),
  ADD INDEX idx_link_apply_url_status (site_url, status, deleted);
