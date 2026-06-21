# TechNote 快速交接说明

这份文档用于下一位开发者或 AI 快速接手。它只记录“下一步怎么启动、怎么验证、哪些事情还没有跑过”，不替代完整的 `README.md`、`docs/DEMO_CHECKLIST.md` 和 `docs/PROGRESS.md`。

## 1. 当前边界

- 当前项目目录不是 Git 仓库，`git status --short` 无法使用。
- 目前只做过源码阅读、字段核对、文档更新和静态搜索。
- 未运行 `npm install`、`npm run build`、`npm run dev`。
- 未运行 Maven 构建、测试或后端启动命令。
- 未真实连接 MySQL，未导入 SQL，未做浏览器截图和交互验收。

原因：项目规则要求安装、构建、启动服务、依赖解析和数据库操作必须先单独获得允许。

## 2. 环境版本

项目按当前机器版本整理：

- Java 21.0.9 LTS
- Spring Boot 3.3.7
- MySQL 8.0.44
- Node 24.11.0
- npm 11.6.1

后端默认配置在 `backend/src/main/resources/application.yml`：

- 服务端口：`8080`
- 后端 context-path：`/api`
- 数据库默认地址：`MYSQL_URL=jdbc:mysql://localhost:3306/technote?...`
- 数据库默认账号密码：`MYSQL_USERNAME=root` / `MYSQL_PASSWORD=root`
- 公开站点地址：`TECHNOTE_PUBLIC_SITE_URL=http://localhost:5173`
- 上传目录默认值：`TECHNOTE_UPLOAD_ROOT_PATH=uploads`

这些环境变量不设置时会使用默认值；正式部署时建议覆盖数据库账号密码和公开站点地址。

## 3. 数据库脚本顺序

全新数据库：

```text
database/technote_schema.sql
database/seed_demo_data.sql
```

已有数据库按需升级：

```text
database/upgrade_article_interaction.sql
database/upgrade_dashboard_statistics.sql
database/upgrade_link_apply.sql
database/upgrade_public_submit_guard.sql
database/seed_demo_data.sql
```

注意：

- `seed_demo_data.sql` 是可选演示数据脚本，可重复执行，但不要导入生产库。
- `upgrade_link_apply.sql` 会增加唯一约束，执行前需要先确认正式友链网址和待审核申请网址没有重复。
- 全新数据库已经包含升级脚本里的结构，不需要重复执行升级脚本。

## 4. 建议启动顺序

获得明确允许后再执行：

```text
cd backend
mvn spring-boot:run
```

```text
cd frontend
npm install
npm run dev
```

建议随后再执行构建验证：

```text
cd frontend
npm run build
```

后端 Maven 构建或测试也需要单独获得允许后再跑。

## 5. 首轮验收路径

公开端优先看：

- `/`
- `/articles/{slug}`
- `/columns`
- `/columns/{slug}`
- `/archives`
- `/search?keyword=Spring`
- `/links`
- `/guestbook`
- `/about`
- `/api/rss.xml`
- `/api/sitemap.xml`
- `/api/robots.txt`

后台优先看：

- `/admin/login`
- `/admin`
- `/admin/articles`
- `/admin/articles/new`
- `/admin/auto-drafts`
- `/admin/columns`
- `/admin/taxonomy`
- `/admin/links`
- `/admin/link-applications`
- `/admin/media`
- `/admin/moderation`
- `/admin/public-submit-guard`
- `/admin/recycle`
- `/admin/operation-logs`
- `/admin/profile`
- `/admin/settings`

默认管理员来自 `database/technote_schema.sql`：

```text
username: admin
password: 123456
```

## 6. 已重点静态核对

- 前端 API 路径与后端 Controller 的主要路径已对齐，前端 Axios 基础路径是 `/api`。
- 后台菜单与路由入口已覆盖主要页面。
- 新增表 `blog_article_like`、`blog_article_view_daily`、`blog_link_apply`、`blog_guard_setting` 与实体/Mapper/Service 字段做过静态核对。
- 点赞、每日阅读趋势、友链申请、公开风控的事务、幂等和字段映射做过静态扫描。
- 前端公开表单、文章编辑器、Markdown 渲染、基础 UI 组件和路径别名做过静态扫描。
- 未发现必须立即修改的业务源码问题。

## 7. 仍需重点验证

- Maven 编译能否通过，尤其是 Mapper 注解 SQL、MyBatis-Plus 泛型和配置绑定。
- `npm run build` 能否通过，尤其是 Vue 模板、组件事件类型和 ECharts。
- SQL 在 MySQL 8.0.44 下是否全部可执行，特别是生成列和唯一索引。
- 浏览器真实交互：登录、文章编辑、自动草稿、图片上传、点赞、评论、留言、友链申请、审核、RSS/sitemap/robots。
- 移动端真实截图：公开导航、文章目录、后台抽屉导航、表格横向滚动、长文本换行。
- `technote.public-site-url` 在部署时必须改成真实前端站点地址，否则 RSS/sitemap/robots 会生成本地链接。

## 8. 推荐下一步

1. 先获得允许后跑前端依赖安装和构建。
2. 再获得允许后跑后端 Maven 构建或启动。
3. 初始化本地 MySQL 演示库并导入 SQL。
4. 按 `docs/DEMO_CHECKLIST.md` 做手动验收。
5. 把实际构建输出、启动结果、失败点补回 `docs/PROGRESS.md`。
