-- 文章匿名点赞关系表。
-- 点赞总数继续保存在 blog_article.like_count，关系表唯一键负责防止同一访客重复点赞。
CREATE TABLE IF NOT EXISTS blog_article_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  visitor_key VARCHAR(64) NOT NULL COMMENT '匿名访客标识',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_article_like_visitor (article_id, visitor_key),
  KEY idx_article_like_visitor (visitor_key, create_time)
) ENGINE=InnoDB COMMENT='文章匿名点赞关系表';

