CREATE DATABASE IF NOT EXISTS technote DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE technote;

-- 说明：
-- 1. 当前设计保留业务外键字段，但不强制建立数据库外键，避免软删除、回收站恢复和演示环境导入顺序带来阻塞。
-- 2. 防重复、状态过滤、公开内容边界主要依赖唯一索引、状态字段和业务层条件更新。
-- 3. 所有对访客可见的查询都必须过滤 deleted = 0、status = 1、visibility = 1。

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(64) NOT NULL COMMENT '登录用户名',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
  nickname VARCHAR(64) NOT NULL COMMENT '显示昵称',
  avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  bio TEXT COMMENT '个人简介',
  role_code VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT '角色编码：ADMIN管理员',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_status (status, deleted)
) ENGINE=InnoDB COMMENT='管理员用户表';

CREATE TABLE blog_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  slug VARCHAR(80) NOT NULL COMMENT '分类访问标识',
  description VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1显示',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  UNIQUE KEY uk_category_slug (slug, deleted),
  KEY idx_category_status_sort (status, sort_order, deleted)
) ENGINE=InnoDB COMMENT='文章分类表';

CREATE TABLE blog_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(64) NOT NULL COMMENT '标签名称',
  slug VARCHAR(80) NOT NULL COMMENT '标签访问标识',
  color VARCHAR(32) DEFAULT NULL COMMENT '标签颜色',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  UNIQUE KEY uk_tag_slug (slug, deleted),
  KEY idx_tag_name (name, deleted)
) ENGINE=InnoDB COMMENT='文章标签表';

CREATE TABLE blog_column (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(100) NOT NULL COMMENT '专栏名称',
  slug VARCHAR(120) NOT NULL COMMENT '专栏访问标识',
  description VARCHAR(500) DEFAULT NULL COMMENT '专栏简介',
  cover_url VARCHAR(500) DEFAULT NULL COMMENT '专栏封面',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1显示',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  UNIQUE KEY uk_column_slug (slug, deleted),
  KEY idx_column_status_sort (status, sort_order, deleted)
) ENGINE=InnoDB COMMENT='专栏表';

CREATE TABLE blog_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(200) NOT NULL COMMENT '文章标题',
  slug VARCHAR(220) NOT NULL COMMENT '文章访问标识',
  summary VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
  content LONGTEXT NOT NULL COMMENT 'Markdown正文',
  cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面图地址',
  category_id BIGINT DEFAULT NULL COMMENT '分类ID',
  column_id BIGINT DEFAULT NULL COMMENT '专栏ID',
  top_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
  visibility TINYINT NOT NULL DEFAULT 1 COMMENT '可见性：0私密 1公开',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1发布 2自动草稿',
  view_count BIGINT NOT NULL DEFAULT 0 COMMENT '阅读量',
  like_count BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  UNIQUE KEY uk_article_slug (slug, deleted),
  KEY idx_article_public_list (visibility, status, top_flag, publish_time, deleted),
  KEY idx_article_category (category_id, status, deleted),
  KEY idx_article_column (column_id, status, deleted),
  FULLTEXT KEY ft_article_title_summary_content (title, summary, content)
) ENGINE=InnoDB COMMENT='文章表';

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

CREATE TABLE blog_article_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  visitor_key VARCHAR(64) NOT NULL COMMENT '匿名访客标识',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_article_like_visitor (article_id, visitor_key),
  KEY idx_article_like_visitor (visitor_key, create_time)
) ENGINE=InnoDB COMMENT='文章匿名点赞关系表';

CREATE TABLE blog_article_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_article_tag (article_id, tag_id),
  KEY idx_article_tag_tag (tag_id)
) ENGINE=InnoDB COMMENT='文章标签关联表';

CREATE TABLE blog_column_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  column_id BIGINT NOT NULL COMMENT '专栏ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '专栏内排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_column_article (column_id, article_id),
  KEY idx_column_article_sort (column_id, sort_order)
) ENGINE=InnoDB COMMENT='专栏文章关联表';

CREATE TABLE blog_article_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  title VARCHAR(200) NOT NULL COMMENT '版本标题',
  summary VARCHAR(500) DEFAULT NULL COMMENT '版本摘要',
  content LONGTEXT NOT NULL COMMENT '版本正文',
  version_no INT NOT NULL COMMENT '版本号，从1递增',
  version_remark VARCHAR(255) DEFAULT NULL COMMENT '版本备注',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_article_version_no (article_id, version_no),
  KEY idx_article_version_article (article_id, create_time)
) ENGINE=InnoDB COMMENT='文章历史版本表';

CREATE TABLE blog_article_auto_draft (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  draft_key VARCHAR(80) NOT NULL COMMENT '前端草稿键，同一用户内唯一',
  article_id BIGINT DEFAULT NULL COMMENT '关联文章ID，新文章草稿为空',
  title VARCHAR(200) DEFAULT NULL COMMENT '草稿标题',
  slug VARCHAR(220) DEFAULT NULL COMMENT '草稿访问标识',
  summary VARCHAR(500) DEFAULT NULL COMMENT '草稿摘要',
  content LONGTEXT COMMENT '草稿正文',
  cover_url VARCHAR(500) DEFAULT NULL COMMENT '草稿封面',
  category_id BIGINT DEFAULT NULL COMMENT '分类ID',
  column_id BIGINT DEFAULT NULL COMMENT '专栏ID',
  tag_ids VARCHAR(1000) DEFAULT NULL COMMENT '标签ID JSON数组',
  top_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
  visibility TINYINT NOT NULL DEFAULT 1 COMMENT '可见性：0私密 1公开',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  UNIQUE KEY uk_auto_draft_key_user (draft_key, create_by, deleted),
  KEY idx_auto_draft_article_user (article_id, create_by, deleted)
) ENGINE=InnoDB COMMENT='文章自动草稿表';

CREATE TABLE blog_media (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
  file_path VARCHAR(500) NOT NULL COMMENT '文件访问路径',
  file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  file_ext VARCHAR(32) DEFAULT NULL COMMENT '文件扩展名',
  quote_count INT NOT NULL DEFAULT 0 COMMENT '被内容引用次数',
  upload_by BIGINT DEFAULT NULL COMMENT '上传人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  KEY idx_media_type (mime_type, deleted),
  KEY idx_media_upload_time (upload_by, create_time)
) ENGINE=InnoDB COMMENT='媒体文件表';

CREATE TABLE blog_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
  nickname VARCHAR(64) NOT NULL COMMENT '访客昵称',
  email VARCHAR(128) DEFAULT NULL COMMENT '访客邮箱',
  website VARCHAR(255) DEFAULT NULL COMMENT '访客网站',
  content VARCHAR(1000) NOT NULL COMMENT '评论内容',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1通过 2驳回',
  reply_content VARCHAR(1000) DEFAULT NULL COMMENT '管理员回复',
  audit_by BIGINT DEFAULT NULL COMMENT '审核人',
  audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  KEY idx_comment_article_status (article_id, status, deleted),
  KEY idx_comment_parent (parent_id, deleted)
) ENGINE=InnoDB COMMENT='文章评论表';

CREATE TABLE blog_guestbook (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  nickname VARCHAR(64) NOT NULL COMMENT '访客昵称',
  email VARCHAR(128) DEFAULT NULL COMMENT '访客邮箱',
  content VARCHAR(1000) NOT NULL COMMENT '留言内容',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1通过 2驳回',
  reply_content VARCHAR(1000) DEFAULT NULL COMMENT '管理员回复',
  audit_by BIGINT DEFAULT NULL COMMENT '审核人',
  audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  KEY idx_guestbook_status (status, deleted)
) ENGINE=InnoDB COMMENT='留言表';

CREATE TABLE blog_link (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  site_name VARCHAR(100) NOT NULL COMMENT '网站名称',
  site_url VARCHAR(255) NOT NULL COMMENT '网站地址',
  icon_url VARCHAR(500) DEFAULT NULL COMMENT '网站图标',
  description VARCHAR(255) DEFAULT NULL COMMENT '网站描述',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1显示',
  create_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by BIGINT DEFAULT NULL COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  active_site_url VARCHAR(255) GENERATED ALWAYS AS (CASE WHEN deleted = 0 THEN site_url ELSE NULL END) STORED COMMENT '未删除友链网址，用于并发防重',
  UNIQUE KEY uk_link_active_site_url (active_site_url),
  KEY idx_link_status_sort (status, sort_order, deleted)
) ENGINE=InnoDB COMMENT='友情链接表';

CREATE TABLE blog_link_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  site_name VARCHAR(100) NOT NULL COMMENT '网站名称',
  site_url VARCHAR(255) NOT NULL COMMENT '网站地址',
  icon_url VARCHAR(500) DEFAULT NULL COMMENT '网站图标',
  description VARCHAR(255) DEFAULT NULL COMMENT '网站描述',
  applicant_email VARCHAR(128) DEFAULT NULL COMMENT '申请人邮箱',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1通过 2驳回',
  audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  audit_by BIGINT DEFAULT NULL COMMENT '审核人',
  audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  pending_site_url VARCHAR(255) GENERATED ALWAYS AS (CASE WHEN status = 0 AND deleted = 0 THEN site_url ELSE NULL END) STORED COMMENT '待审核申请网址，用于并发防重',
  UNIQUE KEY uk_link_apply_pending_site_url (pending_site_url),
  KEY idx_link_apply_status (status, deleted),
  KEY idx_link_apply_url_status (site_url, status, deleted)
) ENGINE=InnoDB COMMENT='友链申请表';

CREATE TABLE blog_recycle (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  resource_type VARCHAR(64) NOT NULL COMMENT '资源类型：ARTICLE/CATEGORY/TAG/COLUMN/MEDIA等',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  title VARCHAR(200) DEFAULT NULL COMMENT '资源标题，便于回收站展示',
  content_snapshot LONGTEXT COMMENT '删除前JSON快照',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人',
  delete_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '删除时间',
  UNIQUE KEY uk_recycle_resource (resource_type, resource_id),
  KEY idx_recycle_delete_time (delete_time)
) ENGINE=InnoDB COMMENT='回收站表';

CREATE TABLE blog_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
  module VARCHAR(64) NOT NULL COMMENT '操作模块',
  operation VARCHAR(255) NOT NULL COMMENT '操作内容',
  request_method VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  request_uri VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
  request_params TEXT COMMENT '请求参数',
  ip VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  user_agent VARCHAR(500) DEFAULT NULL COMMENT '浏览器标识',
  success_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：0失败 1成功',
  error_message VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_operation_log_module (module, create_time),
  KEY idx_operation_log_user (user_id, create_time)
) ENGINE=InnoDB COMMENT='操作日志表';

CREATE TABLE blog_setting (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  site_title VARCHAR(100) NOT NULL COMMENT '站点标题',
  site_description VARCHAR(255) DEFAULT NULL COMMENT '站点简介',
  site_keywords VARCHAR(255) DEFAULT NULL COMMENT '站点关键词',
  icp_no VARCHAR(100) DEFAULT NULL COMMENT '备案号',
  author_name VARCHAR(64) DEFAULT NULL COMMENT '博主名称',
  author_avatar VARCHAR(500) DEFAULT NULL COMMENT '博主头像',
  author_profile TEXT COMMENT '博主介绍',
  announcement VARCHAR(500) DEFAULT NULL COMMENT '公告内容',
  about_content LONGTEXT COMMENT '关于页面内容',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='系统设置表';

CREATE TABLE blog_guard_setting (
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

-- 密码字段为 BCrypt 密文，默认明文为 123456；后续如更换密码必须由后端加密后写入。
INSERT INTO sys_user (username, password, nickname, role_code, status)
VALUES ('admin', '$2a$10$3Euq3LYS9vIhXrAzKMVq6OrQqVXbV0M7l3S2F2w3m8Qvhhnbq2VnO', '管理员', 'ADMIN', 1);

INSERT INTO blog_setting (site_title, site_description, site_keywords, author_name, author_profile, about_content)
VALUES ('TechNote', '面向技术创作者的个人内容管理与分享系统', 'TechNote,技术博客,内容管理', 'TechNote Admin', '专注技术沉淀与内容创作。', '欢迎来到 TechNote。');

INSERT INTO blog_guard_setting (
  enabled,
  captcha_enabled,
  captcha_ttl_seconds,
  captcha_max_entries,
  sensitive_word_enabled,
  sensitive_word_message,
  sensitive_words
)
VALUES (1, 1, 300, 2000, 1, '提交内容包含暂不支持发布的词汇，请调整后再提交',
        CONCAT_WS(CHAR(10), '博彩', '贷款', '发票', '代刷', 'spam'));
