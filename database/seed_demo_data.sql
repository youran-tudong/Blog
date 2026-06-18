USE technote;

-- TechNote optional demo data.
-- This script is designed for local showcase environments and can be run more than once.
-- It does not delete existing data; stable slugs, URLs and unique keys are used to avoid duplicates.

INSERT INTO blog_category (name, slug, description, sort_order, status)
SELECT '后端工程', 'backend', 'Spring Boot、数据库、系统设计和工程实践', 10, 1
WHERE NOT EXISTS (
  SELECT 1 FROM blog_category WHERE slug = 'backend' AND deleted = 0
);

INSERT INTO blog_category (name, slug, description, sort_order, status)
SELECT '前端体验', 'frontend', 'Vue、交互体验、组件设计和页面 polish', 20, 1
WHERE NOT EXISTS (
  SELECT 1 FROM blog_category WHERE slug = 'frontend' AND deleted = 0
);

INSERT INTO blog_category (name, slug, description, sort_order, status)
SELECT '效率手册', 'productivity', '写作流程、知识管理和个人工作台', 30, 1
WHERE NOT EXISTS (
  SELECT 1 FROM blog_category WHERE slug = 'productivity' AND deleted = 0
);

INSERT INTO blog_tag (name, slug, color)
SELECT 'Spring Boot', 'spring-boot', '#16a34a'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_tag WHERE slug = 'spring-boot' AND deleted = 0
);

INSERT INTO blog_tag (name, slug, color)
SELECT 'Vue 3', 'vue-3', '#0ea5e9'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_tag WHERE slug = 'vue-3' AND deleted = 0
);

INSERT INTO blog_tag (name, slug, color)
SELECT 'MySQL', 'mysql', '#f97316'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_tag WHERE slug = 'mysql' AND deleted = 0
);

INSERT INTO blog_tag (name, slug, color)
SELECT '工程化', 'engineering', '#7c3aed'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_tag WHERE slug = 'engineering' AND deleted = 0
);

INSERT INTO blog_tag (name, slug, color)
SELECT 'Markdown', 'markdown', '#64748b'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_tag WHERE slug = 'markdown' AND deleted = 0
);

INSERT INTO blog_column (name, slug, description, cover_url, sort_order, status)
SELECT '全栈博客从 0 到 1', 'full-stack-notes', '记录一个前后端分离技术博客的建模、接口和页面体验设计。', NULL, 10, 1
WHERE NOT EXISTS (
  SELECT 1 FROM blog_column WHERE slug = 'full-stack-notes' AND deleted = 0
);

INSERT INTO blog_column (name, slug, description, cover_url, sort_order, status)
SELECT '持续构建日志', 'build-in-public', '把功能拆成小步推进，每一轮都保留可交接的上下文。', NULL, 20, 1
WHERE NOT EXISTS (
  SELECT 1 FROM blog_column WHERE slug = 'build-in-public' AND deleted = 0
);

INSERT INTO blog_article (
  title,
  slug,
  summary,
  content,
  cover_url,
  category_id,
  column_id,
  top_flag,
  visibility,
  status,
  view_count,
  like_count,
  publish_time,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  'Spring Boot 博客架构复盘：从内容模型到公开接口',
  'spring-boot-blog-architecture',
  '用一个技术博客项目串起用户、文章、分类、标签、评论和风控配置，重点关注分层边界与可维护性。',
  CONCAT(
    '# Spring Boot 博客架构复盘', CHAR(10), CHAR(10),
    'TechNote 的后端按 Controller、Service、Mapper、Entity、DTO 分层组织。Controller 只处理路径、参数和统一返回，核心业务放在 ServiceImpl。', CHAR(10), CHAR(10),
    '## 内容模型', CHAR(10), CHAR(10),
    '- 文章负责承载 Markdown 正文、发布状态、公开范围和统计数据。', CHAR(10),
    '- 分类是一对多关系，标签是多对多关系，专栏通过关联表维护排序。', CHAR(10),
    '- 评论、留言、友链申请都走审核状态，避免公开侧直接暴露未审核内容。', CHAR(10), CHAR(10),
    '## 接口边界', CHAR(10), CHAR(10),
    '公开接口只返回公开、已发布、未删除的数据；后台接口通过登录态保护，并记录关键操作日志。', CHAR(10), CHAR(10),
    '```java', CHAR(10),
    '@GetMapping("/public/articles/{slug}")', CHAR(10),
    'public ApiResult<ArticleResp> detail(@PathVariable String slug) {', CHAR(10),
    '    return ApiResult.ok(articleService.getPublicArticle(slug));', CHAR(10),
    '}', CHAR(10),
    '```', CHAR(10), CHAR(10),
    '## 小结', CHAR(10), CHAR(10),
    '项目后续扩展时，优先保持高内聚、低耦合：每个模块只暴露必要能力，跨模块通过清晰 DTO 协作。'
  ),
  NULL,
  c.id,
  col.id,
  1,
  1,
  1,
  328,
  12,
  '2026-06-10 09:30:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-10 09:30:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-10 09:30:00'
FROM blog_category c
LEFT JOIN blog_column col ON col.slug = 'full-stack-notes' AND col.deleted = 0
WHERE c.slug = 'backend'
  AND c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_article WHERE slug = 'spring-boot-blog-architecture' AND deleted = 0
  )
LIMIT 1;

INSERT INTO blog_article (
  title,
  slug,
  summary,
  content,
  cover_url,
  category_id,
  column_id,
  top_flag,
  visibility,
  status,
  view_count,
  like_count,
  publish_time,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  'Vue 3 + shadcn-vue 后台体验：让管理页更像工具',
  'vue-shadcn-admin-experience',
  '后台不是落地页，真正重要的是扫描效率、稳定布局、明确反馈和窄屏可用。',
  CONCAT(
    '# Vue 3 + shadcn-vue 后台体验', CHAR(10), CHAR(10),
    'TechNote 的后台页面围绕高频管理任务设计：列表筛选、批量操作、编辑器、数据概览和设置页。', CHAR(10), CHAR(10),
    '## 页面原则', CHAR(10), CHAR(10),
    '- 表格区域允许横向滚动，整页布局不被撑破。', CHAR(10),
    '- 表单字段保持固定节奏，错误、加载和空状态都在原位置反馈。', CHAR(10),
    '- 图标按钮用于明确动作，复杂操作放进分组区域。', CHAR(10), CHAR(10),
    '## 组件策略', CHAR(10), CHAR(10),
    '项目优先复用本地 shadcn-vue 风格组件，页面内部使用 `gap` 组织间距，避免引入新的 UI 风格。', CHAR(10), CHAR(10),
    '## 移动端', CHAR(10), CHAR(10),
    '后台在窄屏下使用抽屉导航和 sticky 顶栏，保留表格、编辑、审核这些核心管理能力。'
  ),
  NULL,
  c.id,
  col.id,
  0,
  1,
  1,
  256,
  9,
  '2026-06-12 14:20:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-12 14:20:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-12 14:20:00'
FROM blog_category c
LEFT JOIN blog_column col ON col.slug = 'full-stack-notes' AND col.deleted = 0
WHERE c.slug = 'frontend'
  AND c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_article WHERE slug = 'vue-shadcn-admin-experience' AND deleted = 0
  )
LIMIT 1;

INSERT INTO blog_article (
  title,
  slug,
  summary,
  content,
  cover_url,
  category_id,
  column_id,
  top_flag,
  visibility,
  status,
  view_count,
  like_count,
  publish_time,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  'MySQL 索引与归档热力图：一次查询优化记录',
  'mysql-index-and-content-archive',
  '从 Java 内存分组切换到 SQL 按日期聚合，让年度写作热力图更适合后续数据增长。',
  CONCAT(
    '# MySQL 索引与归档热力图', CHAR(10), CHAR(10),
    '归档热力图的核心数据是每天发布了多少公开文章。早期实现可以直接读取列表后在 Java 中分组，但数据量增长后更适合交给数据库聚合。', CHAR(10), CHAR(10),
    '## 查询条件', CHAR(10), CHAR(10),
    '公开侧统计必须同时满足 `deleted = 0`、`status = 1`、`visibility = 1`，避免草稿、私密文章和回收站内容进入公开视图。', CHAR(10), CHAR(10),
    '## 聚合方式', CHAR(10), CHAR(10),
    '使用 `DATE(COALESCE(publish_time, update_time, create_time))` 作为统计日期，可以和归档兜底规则保持一致。', CHAR(10), CHAR(10),
    '```sql', CHAR(10),
    'SELECT DATE(COALESCE(publish_time, update_time, create_time)) AS publish_date, COUNT(*) AS count', CHAR(10),
    'FROM blog_article', CHAR(10),
    'WHERE deleted = 0 AND status = 1 AND visibility = 1', CHAR(10),
    'GROUP BY DATE(COALESCE(publish_time, update_time, create_time));', CHAR(10),
    '```'
  ),
  NULL,
  c.id,
  col.id,
  0,
  1,
  1,
  184,
  7,
  '2026-06-13 18:05:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-13 18:05:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-13 18:05:00'
FROM blog_category c
LEFT JOIN blog_column col ON col.slug = 'build-in-public' AND col.deleted = 0
WHERE c.slug = 'backend'
  AND c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_article WHERE slug = 'mysql-index-and-content-archive' AND deleted = 0
  )
LIMIT 1;

INSERT INTO blog_article (
  title,
  slug,
  summary,
  content,
  cover_url,
  category_id,
  column_id,
  top_flag,
  visibility,
  status,
  view_count,
  like_count,
  publish_time,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  'Markdown 写作工作流：把想法稳定沉淀成文章',
  'markdown-writing-workflow',
  '一套适合技术博客的写作流程：草稿、预览、版本、导出和归档。',
  CONCAT(
    '# Markdown 写作工作流', CHAR(10), CHAR(10),
    '好的写作系统不只负责保存正文，还要让作者能安全试错、回滚版本，并在发布后继续复盘。', CHAR(10), CHAR(10),
    '## 从草稿开始', CHAR(10), CHAR(10),
    '编辑器自动草稿负责兜底，正式保存时再生成文章版本，避免灵感和历史记录互相覆盖。', CHAR(10), CHAR(10),
    '## 发布之后', CHAR(10), CHAR(10),
    '公开文章进入归档、搜索、热门排行和相关推荐。读者可以点赞、评论，也可以通过留言板提供反馈。', CHAR(10), CHAR(10),
    '## 导出', CHAR(10), CHAR(10),
    'Markdown 导出保留 front matter，方便迁移到静态站点、知识库或其他内容平台。'
  ),
  NULL,
  c.id,
  col.id,
  0,
  1,
  1,
  143,
  5,
  '2026-06-15 08:45:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-15 08:45:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-15 08:45:00'
FROM blog_category c
LEFT JOIN blog_column col ON col.slug = 'build-in-public' AND col.deleted = 0
WHERE c.slug = 'productivity'
  AND c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_article WHERE slug = 'markdown-writing-workflow' AND deleted = 0
  )
LIMIT 1;

INSERT INTO blog_article (
  title,
  slug,
  summary,
  content,
  cover_url,
  category_id,
  column_id,
  top_flag,
  visibility,
  status,
  view_count,
  like_count,
  publish_time,
  create_by,
  create_time,
  update_by,
  update_time
)
SELECT
  '草稿：可观测性与接口耗时记录方案',
  'draft-api-observability-plan',
  '后台草稿示例，用于展示文章管理列表中的草稿状态。',
  CONCAT(
    '# 可观测性与接口耗时记录方案', CHAR(10), CHAR(10),
    '这是一篇后台草稿，用于展示草稿、私密和未发布内容不会出现在公开侧列表中。'
  ),
  NULL,
  c.id,
  NULL,
  0,
  0,
  0,
  0,
  0,
  NULL,
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-15 10:00:00',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-15 10:00:00'
FROM blog_category c
WHERE c.slug = 'backend'
  AND c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_article WHERE slug = 'draft-api-observability-plan' AND deleted = 0
  )
LIMIT 1;

INSERT IGNORE INTO blog_article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM (
  SELECT 'spring-boot-blog-architecture' AS article_slug, 'spring-boot' AS tag_slug
  UNION ALL SELECT 'spring-boot-blog-architecture', 'engineering'
  UNION ALL SELECT 'vue-shadcn-admin-experience', 'vue-3'
  UNION ALL SELECT 'vue-shadcn-admin-experience', 'engineering'
  UNION ALL SELECT 'mysql-index-and-content-archive', 'mysql'
  UNION ALL SELECT 'mysql-index-and-content-archive', 'spring-boot'
  UNION ALL SELECT 'markdown-writing-workflow', 'markdown'
  UNION ALL SELECT 'markdown-writing-workflow', 'engineering'
) rel
JOIN blog_article a ON a.slug = rel.article_slug AND a.deleted = 0
JOIN blog_tag t ON t.slug = rel.tag_slug AND t.deleted = 0;

INSERT IGNORE INTO blog_column_article (column_id, article_id, sort_order)
SELECT col.id, a.id, rel.sort_order
FROM (
  SELECT 'full-stack-notes' AS column_slug, 'spring-boot-blog-architecture' AS article_slug, 10 AS sort_order
  UNION ALL SELECT 'full-stack-notes', 'vue-shadcn-admin-experience', 20
  UNION ALL SELECT 'build-in-public', 'mysql-index-and-content-archive', 10
  UNION ALL SELECT 'build-in-public', 'markdown-writing-workflow', 20
) rel
JOIN blog_column col ON col.slug = rel.column_slug AND col.deleted = 0
JOIN blog_article a ON a.slug = rel.article_slug AND a.deleted = 0;

INSERT INTO blog_article_view_daily (article_id, stat_date, view_count)
SELECT a.id, views.stat_date, views.view_count
FROM (
  SELECT 'spring-boot-blog-architecture' AS article_slug, DATE('2026-06-10') AS stat_date, 42 AS view_count
  UNION ALL SELECT 'spring-boot-blog-architecture', DATE('2026-06-11'), 56
  UNION ALL SELECT 'spring-boot-blog-architecture', DATE('2026-06-12'), 63
  UNION ALL SELECT 'vue-shadcn-admin-experience', DATE('2026-06-12'), 38
  UNION ALL SELECT 'vue-shadcn-admin-experience', DATE('2026-06-13'), 47
  UNION ALL SELECT 'mysql-index-and-content-archive', DATE('2026-06-13'), 31
  UNION ALL SELECT 'mysql-index-and-content-archive', DATE('2026-06-14'), 52
  UNION ALL SELECT 'markdown-writing-workflow', DATE('2026-06-15'), 29
) views
JOIN blog_article a ON a.slug = views.article_slug AND a.deleted = 0
ON DUPLICATE KEY UPDATE view_count = VALUES(view_count);

INSERT INTO blog_comment (
  article_id,
  parent_id,
  nickname,
  email,
  website,
  content,
  status,
  reply_content,
  audit_by,
  audit_time,
  create_time,
  update_time
)
SELECT
  a.id,
  0,
  '林间写代码',
  'reader@example.com',
  'https://example.com',
  '这篇架构拆分很清楚，尤其是公开接口和后台接口的边界。',
  1,
  '谢谢反馈，后续会继续补充部署和构建验证记录。',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-10 12:30:00',
  '2026-06-10 12:00:00',
  '2026-06-10 12:30:00'
FROM blog_article a
WHERE a.slug = 'spring-boot-blog-architecture'
  AND a.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_comment c
    WHERE c.article_id = a.id
      AND c.nickname = '林间写代码'
      AND c.content = '这篇架构拆分很清楚，尤其是公开接口和后台接口的边界。'
      AND c.deleted = 0
  )
LIMIT 1;

INSERT INTO blog_comment (
  article_id,
  parent_id,
  nickname,
  email,
  website,
  content,
  status,
  audit_by,
  audit_time,
  create_time,
  update_time
)
SELECT
  a.id,
  parent.id,
  'TechNote Admin',
  NULL,
  NULL,
  '这条回复用于展示两层评论结构。',
  1,
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-10 13:10:00',
  '2026-06-10 13:00:00',
  '2026-06-10 13:10:00'
FROM blog_article a
JOIN blog_comment parent ON parent.article_id = a.id
  AND parent.parent_id = 0
  AND parent.nickname = '林间写代码'
  AND parent.content = '这篇架构拆分很清楚，尤其是公开接口和后台接口的边界。'
  AND parent.deleted = 0
WHERE a.slug = 'spring-boot-blog-architecture'
  AND a.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_comment c
    WHERE c.article_id = a.id
      AND c.parent_id = parent.id
      AND c.nickname = 'TechNote Admin'
      AND c.content = '这条回复用于展示两层评论结构。'
      AND c.deleted = 0
  )
LIMIT 1;

INSERT INTO blog_comment (
  article_id,
  parent_id,
  nickname,
  email,
  website,
  content,
  status,
  create_time,
  update_time
)
SELECT
  a.id,
  0,
  '待审核读者',
  'pending@example.com',
  NULL,
  '这是一条待审核评论，用于展示后台评论审核列表。',
  0,
  '2026-06-15 09:20:00',
  '2026-06-15 09:20:00'
FROM blog_article a
WHERE a.slug = 'markdown-writing-workflow'
  AND a.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM blog_comment c
    WHERE c.article_id = a.id
      AND c.nickname = '待审核读者'
      AND c.content = '这是一条待审核评论，用于展示后台评论审核列表。'
      AND c.deleted = 0
  )
LIMIT 1;

INSERT INTO blog_guestbook (
  nickname,
  email,
  content,
  status,
  reply_content,
  audit_by,
  audit_time,
  create_time,
  update_time
)
SELECT
  '南山访客',
  'hello@example.com',
  '站点的归档和搜索很适合用来回看长期写作轨迹。',
  1,
  '感谢来访，欢迎继续留言交流。',
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '2026-06-14 20:10:00',
  '2026-06-14 19:40:00',
  '2026-06-14 20:10:00'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_guestbook
  WHERE nickname = '南山访客'
    AND content = '站点的归档和搜索很适合用来回看长期写作轨迹。'
    AND deleted = 0
);

INSERT INTO blog_guestbook (
  nickname,
  email,
  content,
  status,
  create_time,
  update_time
)
SELECT
  '待审核留言',
  'guest-pending@example.com',
  '这是一条待审核留言，用于展示后台留言审核。',
  0,
  '2026-06-15 11:20:00',
  '2026-06-15 11:20:00'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_guestbook
  WHERE nickname = '待审核留言'
    AND content = '这是一条待审核留言，用于展示后台留言审核。'
    AND deleted = 0
);

INSERT INTO blog_link (site_name, site_url, icon_url, description, sort_order, status, create_by, update_by)
SELECT
  'Spring',
  'https://spring.io/',
  NULL,
  'Spring 官方站点，适合查阅 Spring Boot 生态资料。',
  10,
  1,
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1))
WHERE NOT EXISTS (
  SELECT 1 FROM blog_link WHERE site_url = 'https://spring.io/' AND deleted = 0
);

INSERT INTO blog_link (site_name, site_url, icon_url, description, sort_order, status, create_by, update_by)
SELECT
  'Vue',
  'https://vuejs.org/',
  NULL,
  'Vue 官方文档，前端实现和组合式 API 的主要参考。',
  20,
  1,
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1))
WHERE NOT EXISTS (
  SELECT 1 FROM blog_link WHERE site_url = 'https://vuejs.org/' AND deleted = 0
);

INSERT INTO blog_link (site_name, site_url, icon_url, description, sort_order, status, create_by, update_by)
SELECT
  'shadcn-vue',
  'https://www.shadcn-vue.com/',
  NULL,
  'shadcn-vue 组件文档，适合保持后台组件风格一致。',
  30,
  1,
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1))
WHERE NOT EXISTS (
  SELECT 1 FROM blog_link WHERE site_url = 'https://www.shadcn-vue.com/' AND deleted = 0
);

INSERT INTO blog_link_apply (
  site_name,
  site_url,
  icon_url,
  description,
  applicant_email,
  status,
  create_time,
  update_time
)
SELECT
  '示例申请站点',
  'https://apply.example.com/',
  NULL,
  '待审核友链申请，用于展示后台审核流程。',
  'apply@example.com',
  0,
  '2026-06-15 12:00:00',
  '2026-06-15 12:00:00'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_link_apply
  WHERE site_url = 'https://apply.example.com/'
    AND status = 0
    AND deleted = 0
);

INSERT INTO blog_operation_log (
  user_id,
  module,
  operation,
  request_method,
  request_uri,
  request_params,
  ip,
  user_agent,
  success_flag,
  create_time
)
SELECT
  (SELECT COALESCE((SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1), 1)),
  '演示数据',
  '导入 TechNote 演示内容',
  'SQL',
  'database/seed_demo_data.sql',
  NULL,
  '127.0.0.1',
  'manual-seed',
  1,
  '2026-06-15 12:30:00'
WHERE NOT EXISTS (
  SELECT 1 FROM blog_operation_log
  WHERE module = '演示数据'
    AND operation = '导入 TechNote 演示内容'
    AND request_uri = 'database/seed_demo_data.sql'
);
