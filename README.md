# TechNote 技术博客系统

TechNote 是一个前后端分离的个人技术博客项目，目标是完成可展示、可继续扩展的内容管理系统。

- 前端：Vue 3 + TypeScript + Vite + Tailwind CSS + shadcn-vue 风格源码组件
- 后端：Spring Boot 3.3.7 + Java 21 + MyBatis-Plus + Sa-Token
- 数据库：MySQL 8.0.44

## 当前完成度

已完成的主要模块：

- 管理员登录：登录、当前用户、退出、后台路由登录校验。
- 个人中心：管理员资料维护、头像预览、邮箱与简介更新、旧密码校验和修改密码后重新登录。
- 分类标签：后台 CRUD、前台公开列表、文章筛选。
- 文章管理：后台分页、新建、编辑、删除、置顶、公开/私密切换、Markdown 编辑，列表接口使用无正文摘要响应并批量装配分类、专栏、标签信息。
- 批量操作：文章批量设公开/私密、批量修改分类、批量移入回收站。
- 文章版本：编辑前快照、版本列表、版本详情、版本回滚、Markdown 预览。
- 自动草稿：独立草稿表、编辑页 30 秒自动保存、打开时提示恢复、正式保存后清理草稿，并提供后台草稿维护与过期清理。
- 文章导出：后台支持单篇和批量导出 Markdown 文件，便于内容资产迁移。
- 专栏模块：后台专栏 CRUD、专栏文章关联、专栏内排序、文章所属专栏同步、前台专栏列表和专栏详情页。
- 站点设置：后台站点信息维护、前台布局和关于页读取。
- 归档友链：公开归档、公开友链、访客友链申请、后台申请审核与正式友链管理。
- 年度写作热力图：归档页按年份展示每日公开文章发布数量，后端按日期 SQL 聚合公开已发布文章。
- 全文搜索：前台搜索结果页、标题/片段关键词高亮、顶部搜索框和 Ctrl+K 快捷入口。
- 内容发现：公开侧栏热门文章排行、文章详情相关文章推荐、后台数据概览热门公开文章排行。
- 文章互动：匿名访客幂等点赞/取消点赞、点赞总数、两层评论回复、后台评论审核；公开评论不返回访客邮箱。
- 防滥用：公开评论、留言、友链申请和点赞写操作提供单机内存限流；评论、留言和友链申请额外接入数学验证码与配置化敏感词过滤，后台可维护公开提交风控策略。
- 媒体库：图片上传、文章封面/正文插图、媒体分页、引用计数、未引用媒体删除。
- 回收站：文章删除快照、恢复、永久删除。
- 操作日志：后台操作落库、敏感参数脱敏、日志查询页。
- 数据概览：后台真实统计接口、文章/草稿/阅读/待审互动统计、近 7/30 天阅读与发布趋势、分类占比、热门排行、最近文章和最近操作。
- Markdown 渲染：轻量安全渲染，过滤原始 HTML，只允许安全链接和图片地址；代码块支持语言标识、行号、轻量高亮和一键复制；文章详情支持目录锚点和回到顶部。
- 公开页体验：首页、专栏、搜索、归档、友链和留言板使用统一可操作空状态，未知地址进入可返回首页或搜索的 404 页面。
- 移动端适配：移动端保留公开主导航和内容筛选入口，文章目录前置，分页按钮与长文本适配窄屏展示；后台支持移动端抽屉导航、sticky 顶部栏和表格内部横向滚动。
- 交互反馈：公开异步页面统一加载状态与可访问错误提示，点赞、评论和留言支持加载/提交状态与防重复提交。

## 目录结构

```text
TechNote
├─ backend
│  ├─ pom.xml
│  └─ src/main
├─ frontend
│  ├─ package.json
│  └─ src
├─ database
│  ├─ technote_schema.sql
│  ├─ upgrade_article_interaction.sql
│  ├─ upgrade_dashboard_statistics.sql
│  ├─ upgrade_link_apply.sql
│  ├─ upgrade_public_submit_guard.sql
│  └─ seed_demo_data.sql
├─ docs
│  ├─ PROGRESS.md
│  ├─ DEMO_CHECKLIST.md
│  ├─ HANDOFF.md
│  └─ COMPLETION_AUDIT.md
├─ .gitignore
└─ README.md
```

## 运行说明

当前机器版本依据：

- Java 21.0.9 LTS
- MySQL 8.0.44
- Node 24.11.0
- npm 11.6.1

需要运行时：

```bash
cd backend
mvn spring-boot:run
```

数据库准备：

```text
全新数据库：
database/technote_schema.sql
```

已有数据库按需执行升级脚本，建议顺序：

```text
database/upgrade_article_interaction.sql
database/upgrade_dashboard_statistics.sql
database/upgrade_link_apply.sql
database/upgrade_public_submit_guard.sql
```

其中 `upgrade_link_apply.sql` 会增加唯一约束，执行前需要先确认正式友链网址和待审核申请网址没有重复；全新数据库已经包含这些表结构，不需要再执行升级脚本。

```bash
cd frontend
npm install
npm run dev
```

本轮未运行 Maven/npm 构建或开发服务器，因为当前 `AGENTS.md` 要求构建、安装、启动服务必须先单独询问。当前验证以源码阅读、字段对照和静态检查为主。

## 公开订阅与站点地图

后端提供公开 XML 输出：

```text
/api/rss.xml
/api/sitemap.xml
/api/robots.txt
```

RSS、sitemap 和 robots 中的公开链接根据 `application.yml` 的 `technote.public-site-url` 生成，默认是 `http://localhost:5173`。部署到正式域名时需要把该配置改为前端站点地址；如需让搜索引擎直接访问根路径 `/robots.txt` 和 `/sitemap.xml`，请在前端静态服务或网关中转发到对应后端接口。

前端会在运行时维护基础 `<head>` 元信息，包括页面标题、description、keywords、Open Graph、canonical、RSS alternate 和 JSON-LD 结构化数据。普通公开页会输出 WebSite/SearchAction，文章详情页会输出 BlogPosting。当前属于客户端基础 SEO，不等同于服务端渲染。

## 演示数据

项目提供一份可选演示数据脚本：

```text
database/seed_demo_data.sql
```

它会补充分组、标签、专栏、公开文章、草稿、文章标签关系、专栏文章关系、每日阅读趋势、评论、留言、友链、友链申请和一条操作日志，方便展示首页、文章详情、归档热力图、热门排行、后台审核和数据概览。脚本按 `slug`、URL、内容片段和唯一键做幂等保护，可重复执行；它不会删除现有数据。

注意：本脚本不会自动执行。需要演示数据时，请先执行 `database/technote_schema.sql` 和需要的升级脚本，再按需手动导入 `database/seed_demo_data.sql`。

## 演示验收

手动展示和交接验收可参考：

```text
docs/DEMO_CHECKLIST.md
```

该清单覆盖环境确认、数据库导入、构建启动、公开站点、后台管理、移动端适配和回归风险点。当前未自动运行其中的命令或 SQL，后续需要在获得允许后逐项执行。

更短的接手说明可先阅读：

```text
docs/HANDOFF.md
```

最终完成度静态审计可参考：

```text
docs/COMPLETION_AUDIT.md
```
