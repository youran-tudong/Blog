# TechNote 开发进度交接

## 版本依据

- Java：21.0.9 LTS
- MySQL：8.0.44
- Node：24.11.0
- npm：11.6.1
- 后端：Spring Boot 3.3.7 + Java 21 + MyBatis-Plus + Sa-Token
- 前端：Vue 3 + Vite + TypeScript + Tailwind CSS + shadcn-vue 风格源码组件

## 已完成模块

### 1. 基础工程

- `backend` Spring Boot 工程、统一返回 `ApiResult`、统一异常 `BaseException`、全局异常处理、跨域、MyBatis-Plus 分页、Sa-Token、文件上传配置。
- `frontend` Vue/Vite 工程、Router、Pinia、Axios 封装、Tailwind 主题变量、shadcn-vue 风格组件。
- `database/technote_schema.sql` 包含博客核心表结构。

### 2. 后台与内容模块

- 管理员登录、当前用户、退出。
- 分类/标签后台管理和前台公开查询。
- 文章后台管理、公开列表、公开详情、归档。
- 文章批量设公开/私密、批量修改分类、批量移入回收站。
- Markdown 轻量安全渲染，代码块支持语言标识、行号、轻量高亮和一键复制。
- 文章历史版本列表、详情和回滚。
- 文章自动草稿保存、恢复、正式保存后清理、后台维护列表和过期清理。
- 文章 Markdown 单篇/批量导出。
- 媒体上传、媒体库、封面上传、正文插图、媒体引用计数。
- 评论/留言提交和后台审核。
- 友链后台管理和前台展示。
- 年度写作热力图。
- 全文搜索结果页、关键词高亮、顶部搜索框和 Ctrl+K 快捷入口。
- 站点设置和关于页。
- 操作日志落库与后台查询。
- 后台数据概览真实统计。
- 文章回收站：删除快照、恢复、永久删除。

### 3. 已完成：专栏模块

后端新增/完善：

- `BlogColumn`、`BlogColumnArticle` 实体。
- `BlogColumnMapper`、`BlogColumnArticleMapper`。
- `ColumnPageQueryReq`、`ColumnSaveReq`、`ColumnArticleSaveReq`、`ColumnResp`、`ColumnArticleResp`。
- `ColumnService`、`ColumnServiceImpl`。
- `AdminColumnController`：`/admin/columns` CRUD、专栏文章关联、排序、移除。
- `PublicColumnController`：`/public/columns`、`/public/columns/{slug}`、`/public/columns/{slug}/articles`。
- `ArticleSaveReq`、`ArticlePageQueryReq`、`PublicArticlePageQueryReq` 支持 `columnId`。
- `ArticleResp` 支持 `columnId`、`columnName`、`columnSlug`。
- `ArticleServiceImpl` 在文章新建/编辑时校验专栏存在，并同步 `blog_article.column_id` 与 `blog_column_article`。
- `RecycleServiceImpl` 永久删除文章时清理 `blog_column_article`，软删除保留专栏关系，方便恢复。

前端新增/完善：

- `frontend/src/api/column.ts`。
- `AdminColumnsPage.vue`：专栏分页、搜索、创建、编辑、删除空专栏、搜索文章加入专栏、专栏内排序、移出文章。
- `ColumnsPage.vue`：前台专栏列表和专栏详情文章分页。
- `AdminLayout.vue` 增加“专栏管理”菜单。
- `PublicLayout.vue` 增加“专栏”导航和左侧专栏列表。
- `AdminArticlesPage.vue` 增加专栏筛选和专栏展示。
- `ArticleEditorPage.vue` 增加专栏选择，保留 Markdown 预览、图片上传、版本历史。
- `HomePage.vue` 支持 `columnId` 过滤并展示文章所属专栏。
- `ArticleDetailPage.vue` 展示所属专栏，并优先跳转到 `/columns/{columnSlug}`。

### 4. 本轮完成：自动草稿和后台数据概览

后端新增/完善：

- `blog_article_auto_draft` 表，用独立草稿表保存编辑器自动草稿，不覆盖正式文章。
- `BlogArticleAutoDraft` 实体、`BlogArticleAutoDraftMapper`。
- `ArticleAutoDraftSaveReq`、`ArticleAutoDraftResp`。
- `ArticleAutoDraftService`、`ArticleAutoDraftServiceImpl`：按当前登录用户保存、查询、物理清理自动草稿；保存时校验文章、分类、专栏、标签存在；标签以 JSON 数组存储。
- `AdminArticleAutoDraftController`：`/admin/article-auto-drafts` 保存、按草稿键查询、按文章查询、删除当前用户草稿。
- `BlogArticleMapper.selectTotalViewCount()`：统计未删除文章总阅读量。
- `DashboardStatsResp`、`DashboardRecentArticleResp`、`DashboardRecentOperationResp`。
- `DashboardService`、`DashboardServiceImpl`：统计文章、已发布、草稿、自动草稿、阅读量、分类、标签、专栏、媒体、待审评论/留言、操作日志，并返回最近文章和最近操作。
- `AdminDashboardController`：`/admin/dashboard/stats`。

前端新增/完善：

- `frontend/src/api/dashboard.ts`：后台数据概览 API 类型和请求。
- `AdminDashboardPage.vue`：替换硬编码假数据，展示真实统计卡、最近文章表、最近后台操作表，包含加载、错误和空状态。
- `frontend/src/api/article.ts`：自动草稿保存、查询、按文章查询、删除 API。
- `ArticleEditorPage.vue`：新文章生成本地临时草稿键，已有文章使用 `article-{id}`；打开页面检测自动草稿并询问恢复；每 30 秒自动保存；顶部展示自动保存状态；正式保存成功后清理自动草稿并避免重复写入。

### 5. 本轮继续完成：年度写作热力图

后端新增/完善：

- `ArchiveHeatmapDayResp`、`ArchiveHeatmapResp`。
- `ArticleService.getPublicArchiveHeatmap(Integer year)`：按指定年份统计公开文章每日发布数量。
- `PublicArticleController` 新增 `GET /public/articles/archives/heatmap?year=2026`。

前端新增/完善：

- `frontend/src/api/publicArticle.ts` 新增 `ArchiveHeatmap` 类型和 `getPublicArchiveHeatmapApi`。
- `ArchivesPage.vue` 增加年份切换、全年发布总量、12 行月份 × 31 天方格热力图，悬浮显示当天发布数量。

### 6. 本轮继续完成：文章 Markdown 导出

后端新增/完善：

- `ArticleExportBatchReq`：批量导出文章ID请求，单次最多 100 篇。
- `ArticleExportFileResp`：导出文件名和 Markdown 内容。
- `ArticleExportService`、`ArticleExportServiceImpl`：导出单篇/批量文章为 Markdown，包含标题、slug、状态、公开性、分类、专栏、标签、发布时间、更新时间等 front matter。
- `AdminArticleController` 新增：
  - `GET /admin/articles/{id}/export`
  - `POST /admin/articles/export`
- 导出接口返回 `ResponseEntity<byte[]>`，下载场景不包 `ApiResult`，但仍受后台登录校验和操作日志记录保护。

前端新增/完善：

- `frontend/src/api/request.ts`：`blob` / `arraybuffer` 响应跳过统一 JSON 结果校验。
- `frontend/src/api/article.ts`：新增 `exportArticleMarkdownApi`、`exportArticlesMarkdownApi`。
- `AdminArticlesPage.vue`：增加文章复选框、单篇导出按钮、批量导出已选按钮，并解析后端 `Content-Disposition` 文件名下载。

### 7. 本轮继续完成：文章批量操作增强

后端新增/完善：

- `ArticleBatchDeleteReq`、`ArticleBatchVisibilityReq`、`ArticleBatchCategoryReq`。
- `ArticleService` 新增：
  - `batchDeleteArticles(List<Long> articleIds)`
  - `batchUpdateVisibility(List<Long> articleIds, Integer visibility)`
  - `batchUpdateCategory(List<Long> articleIds, Long categoryId)`
- `ArticleServiceImpl` 抽出 `deleteArticleInternal`，批量删除复用单篇删除的回收站快照、标签清理、媒体引用更新逻辑。
- `AdminArticleController` 新增：
  - `POST /admin/articles/batch/delete`
  - `PUT /admin/articles/batch/visibility`
  - `PUT /admin/articles/batch/category`

前端新增/完善：

- `frontend/src/api/article.ts` 新增 `batchDeleteArticlesApi`、`batchUpdateArticleVisibilityApi`、`batchUpdateArticleCategoryApi`。
- `AdminArticlesPage.vue` 在已选文章操作条中增加：设为公开、设为私密、修改分类、移入回收站、清空选择。

### 8. 本轮继续完成：全文搜索与快捷入口

后端新增/完善：

- `PublicArticleSearchReq`：公开文章搜索请求，支持分页、关键词、排序。
- `ArticleSearchResp`：搜索结果响应，只返回标题、slug、摘要、命中片段、分类、专栏、标签、阅读量和时间，不返回整篇正文。
- `ArticleService.searchPublicArticles(PublicArticleSearchReq req)`：搜索标题、摘要、正文、访问标识；仅查询公开已发布文章。
- `PublicArticleController` 新增 `GET /public/articles/search`。

前端新增/完善：

- `frontend/src/api/publicArticle.ts` 新增 `ArticleSearchItem`、`PublicArticleSearchQuery`、`searchPublicArticlesApi`。
- `SearchPage.vue`：搜索结果页，支持按时间/阅读量排序、分页、标题与片段关键词安全高亮。
- `router/index.ts` 增加 `/search` 路由。
- `PublicLayout.vue` 顶部搜索框可直接跳转搜索页，支持 `Ctrl+K` / `Cmd+K` 聚焦搜索框。

### 9. 本轮继续完成：代码块阅读体验

前端新增/完善：

- `frontend/src/lib/markdown.ts`：代码围栏支持解析语言标识，渲染为独立 `figure.markdown-code-block`，保留原有 HTML 转义策略；代码行增加行号、空行标记和轻量词法高亮。
- `frontend/src/lib/markdown.ts`：新增 `copyMarkdownCodeFromEvent`，从渲染后的代码行读取文本并写入剪贴板；优先使用 `navigator.clipboard`，不支持时回退到临时 `textarea` 复制。
- `frontend/src/styles/index.css`：新增 Markdown 代码块全局样式，处理窄屏横向滚动、稳定行号列、语言栏、复制按钮和浅/深色模式 token 颜色。
- `ArticleDetailPage.vue`：公开文章详情正文接入代码复制点击事件。
- `ArticleEditorPage.vue`：后台文章 Markdown 预览和历史版本预览接入代码复制点击事件，方便编辑时直接验证效果。

### 10. 本轮继续完成：自动草稿维护页和过期清理

后端新增/完善：

- `ArticleAutoDraftPageQueryReq`：后台自动草稿分页查询条件，支持关键字、关联文章、创建人和只看孤立草稿。
- `ArticleAutoDraftCleanupReq`、`ArticleAutoDraftCleanupResp`：按保留天数清理过期草稿，默认只清理未关联正式文章的新文章草稿。
- `ArticleAutoDraftListResp`：自动草稿列表响应，只返回正文预览和正文长度，不把完整 Markdown 正文带到列表页。
- `BlogArticleAutoDraftMapper` 新增 `hardDeleteById`、`hardDeleteExpired`，继续使用物理删除，避免逻辑删除记录影响草稿唯一键复用。
- `ArticleAutoDraftService`、`ArticleAutoDraftServiceImpl` 新增分页、按 ID 删除、过期清理逻辑；清理和删除加事务。
- `AdminArticleAutoDraftController` 新增：
  - `GET /admin/article-auto-drafts`
  - `DELETE /admin/article-auto-drafts/id/{id}`
  - `POST /admin/article-auto-drafts/cleanup`

前端新增/完善：

- `frontend/src/api/article.ts` 新增自动草稿分页、按 ID 删除、过期清理 API 类型和请求方法。
- `AdminAutoDraftsPage.vue`：后台自动草稿维护页，支持搜索、只看新文章草稿、分页、单条删除、按保留天数清理过期草稿。
- `router/index.ts` 新增 `/admin/auto-drafts` 路由。
- `AdminLayout.vue` 新增“自动草稿”后台菜单入口。

### 11. 本轮继续完成：文章列表摘要 DTO 拆分

后端新增/完善：

- `ArticleListResp`：文章列表响应 DTO，保留标题、摘要、封面、分类、专栏、标签、状态、阅读量和时间字段，不包含完整 `content`。
- `ArticleResp` 改为继承 `ArticleListResp`，仅详情、编辑、保存、回滚等场景额外返回完整 Markdown 正文。
- `ArticleService.pageArticles`、`ArticleService.pagePublicArticles` 改为返回 `PageResp<ArticleListResp>`。
- `ArchiveMonthResp.articles` 改为 `List<ArticleListResp>`，归档列表不再返回完整正文。
- `AdminArticleController`、`PublicArticleController`、`PublicColumnController` 的分页接口泛型同步为 `ArticleListResp`，接口路径不变。
- `ArticleServiceImpl` 新增 `toArticleListResp` 和 `fillArticleListResp`，列表/详情共用基础字段映射，避免字段不一致。

前端新增/完善：

- `frontend/src/api/article.ts` 拆分 `ArticleListItem` 和 `ArticleItem`，其中 `ArticleItem extends ArticleListItem` 且包含 `content`。
- `pageArticlesApi`、`pagePublicArticlesApi`、`pagePublicColumnArticlesApi`、归档类型改为返回 `ArticleListItem`。
- `AdminArticlesPage.vue`、`AdminColumnsPage.vue`、`HomePage.vue`、`ColumnsPage.vue` 切换到 `ArticleListItem`。
- `ArticleEditorPage.vue`、`ArticleDetailPage.vue` 继续使用 `ArticleItem`，保证编辑器和详情页仍能拿到完整正文。

### 12. 本轮继续完成：公开文章阅读 polish

前端新增/完善：

- `frontend/src/lib/markdown.ts` 新增 `MarkdownHeading`、`extractMarkdownHeadings`，从 Markdown 标题生成目录数据；解析时跳过代码块，避免代码示例里的 `#` 被误识别为标题。
- `frontend/src/lib/markdown.ts` 渲染 `h1` 到 `h4` 时生成稳定 `id`，目录锚点和正文标题使用同一套去重规则。
- `ArticleDetailPage.vue` 增加文章目录卡片，桌面端随正文吸顶，移动端按内容顺序展示。
- `ArticleDetailPage.vue` 增加固定“回到顶部”按钮，并保留原有代码块复制事件。
- `ArticleDetailPage.vue` 重写为正常 UTF-8 中文文案，减少此前控制台显示乱码带来的维护风险。

### 13. 本轮继续完成：公开页统一空状态和 404 页面

前端新增/完善：

- `frontend/src/components/EmptyState.vue`：新增可复用空状态组件，统一图标、标题、说明和主次操作入口，操作目标支持 Vue Router 路由对象或路径。
- `NotFoundPage.vue`：未知地址展示可操作 404 页面，可返回首页或进入搜索页。
- `HomePage.vue`：区分“站点暂无文章”和“筛选无结果”，分别提供留言板、清空筛选和归档入口。
- `ColumnsPage.vue`：专栏列表为空、专栏文章为空时提供返回专栏列表、清空搜索或浏览首页的操作入口。
- `SearchPage.vue`：未输入关键词和搜索无结果时提供清晰提示与返回入口。
- `ArchivesPage.vue`、`LinksPage.vue`、`GuestbookPage.vue`：统一公开页空状态，并提供与当前场景相关的下一步操作。

### 14. 本轮继续完成：公开页面移动端适配

前端新增/完善：

- `PublicLayout.vue`：移动端增加可横向滚动的公开主导航；分类、专栏和标签不再随桌面侧栏隐藏，窄屏仍可进入对应筛选和内容页面。
- `PublicLayout.vue`：缩小移动端页边距和顶部栏间距，站点名称支持安全截断，主题按钮继续保持稳定尺寸。
- `frontend/src/components/PublicPagination.vue`：抽取公开页面复用分页组件，窄屏下按钮使用双列满宽布局，桌面端恢复横向紧凑布局。
- `HomePage.vue`、`ColumnsPage.vue`、`SearchPage.vue`：接入复用分页组件，空数据和接口错误时不展示无意义分页；文章标题与摘要支持长文本断行。
- `ArticleDetailPage.vue`：移动端文章目录前置到正文之前；标题字号响应式调整；目录锚点适配移动端双层顶部导航高度；正文、评论和回复支持长文本断行。
- `ArchivesPage.vue`、`GuestbookPage.vue`、`AboutPage.vue`、`EmptyState.vue`：补充长标题、Markdown、留言和空状态文案的窄屏断行保护。

### 15. 本轮继续完成：公开页面加载状态与交互反馈

前端新增/完善：

- `frontend/src/components/LoadingState.vue`：新增可复用加载状态组件，使用明确加载文案、旋转图标、`role="status"` 和 `aria-live`，不依赖嵌套卡片。
- `frontend/src/components/ui/alert/Alert.vue`：成功提示使用状态播报，失败提示使用警告角色，统一公开页面错误和提交结果的可访问反馈。
- `HomePage.vue`、`ColumnsPage.vue`、`SearchPage.vue`：首次进入和切换筛选时显示加载状态，搜索按钮显示稳定宽度的加载中反馈；请求开始时清理旧结果，避免旧数据伪装成新筛选结果。
- `ArticleDetailPage.vue`：正文与评论改为独立加载，正文成功后立即展示；评论加载失败不再导致整篇文章失败；评论提交增加必填校验、提交中状态和防重复提交。
- `GuestbookPage.vue`：拆分列表加载错误与表单提交错误，增加首次加载状态、必填校验、提交中状态和防重复提交。
- `ArchivesPage.vue`：归档列表与年度热力图独立加载，任一区域失败不阻塞另一区域；切换年份时清理旧热力图并显示加载反馈。
- `LinksPage.vue`、`AboutPage.vue`：补齐首次加载状态，避免请求完成前闪现空内容或默认正文。
- 首页、搜索、专栏和文章详情增加轻量请求序号保护，快速切换路由或筛选时只接收最新请求结果。

### 16. 本轮继续完成：友情链接申请与后台审核

数据库新增/完善：

- `blog_link_apply` 增加审核备注、待审核网址生成列和唯一索引，保证并发提交时同一网址最多存在一条未删除待审核申请。
- `blog_link` 增加未删除网址生成列和唯一索引，保证并发审核或后台维护时同一网址最多存在一条未删除正式友链。
- `database/upgrade_link_apply.sql`：为已有数据库提供友链申请审核字段和并发防重约束升级脚本；执行前需先排查重复未删除友链和重复待审核申请。

后端新增/完善：

- 新增 `BlogLinkApply`、`BlogLinkApplyMapper`、申请提交/审核请求、申请响应和 `LinkApplyService` 独立领域模块。
- 新增公开提交接口 `POST /public/link-applies`，提交后默认进入待审核状态；校验站点网址、图标网址和联系邮箱，拒绝已有正式友链或重复待审核申请。
- 新增后台申请接口：`GET /admin/link-applies`、`PUT /admin/link-applies/{id}/audit`、`DELETE /admin/link-applies/{id}`。
- 审核使用事务和 `id + PENDING` 条件更新保证幂等；审核通过后在同一事务中创建隐藏正式友链，创建失败会回滚审核状态。
- 正式友链新增/修改补充重复网址校验和重复键异常转换，数据库唯一约束负责并发窗口兜底。

前端新增/完善：

- `frontend/src/api/link.ts`：补齐公开申请、后台分页、审核和删除 API 类型与请求方法。
- `LinksPage.vue`：新增可折叠友链申请表单，提供必填、邮箱、HTTP(S) 地址校验、提交中状态、防重复提交和结果反馈。
- `AdminLinkApplicationsPage.vue`：新增独立友链申请审核页，支持状态筛选、分页、站点访问、审核备注、通过、驳回和删除；驳回时要求填写原因。
- `router/index.ts` 和 `AdminLayout.vue`：新增 `/admin/link-applications` 路由与“友链申请”后台菜单。

### 17. 本轮继续完成：热门文章与相关文章推荐

后端新增/完善：

- `ArticleService`、`ArticleServiceImpl` 新增公开热门文章和相关文章查询能力；热门文章按阅读量、发布时间排序，相关文章按同专栏、同分类、共同标签计算相关度。
- 新增公开接口：
  - `GET /public/articles/popular?limit=5`
  - `GET /public/articles/{slug}/related?limit=4`
- 热门与相关文章接口仅查询公开已发布文章；相关文章排除当前文章，返回数量限制为 1 到 12。
- 热门与推荐列表显式选择列表字段，不从数据库读取完整 Markdown `content`；相关文章候选最多读取 50 条再计算相关度。
- `DashboardPopularArticleResp`：新增后台概览热门文章专用响应，仅包含排行展示所需字段。
- `DashboardServiceImpl`：仪表盘统计增加热门公开文章排行，不暴露私密或草稿文章。

前端新增/完善：

- `frontend/src/api/publicArticle.ts`：新增公开热门文章与相关文章 API。
- `PublicLayout.vue`：右侧博主信息区增加热门文章排行与紧凑加载状态；移动端不再隐藏该区域，会在主内容后继续展示。
- `ArticleDetailPage.vue`：正文与评论之间增加相关文章推荐；推荐、正文、评论分别加载，推荐失败不会阻断正文或评论。
- `frontend/src/api/dashboard.ts`、`AdminDashboardPage.vue`：数据概览新增热门公开文章排行表。

### 18. 本轮继续完成：数据概览趋势与分类占比

数据库新增/完善：

- 新增 `blog_article_view_daily` 每日阅读量聚合表，按 `article_id + stat_date` 唯一，支持并发访问原子累加和按日期范围汇总。
- `database/upgrade_dashboard_statistics.sql`：为已有数据库提供每日阅读统计表升级脚本；历史累计 `view_count` 无法可靠拆分到过去日期，因此阅读趋势从升级后的新访问开始记录。

后端新增/完善：

- 新增 `BlogArticleViewDaily`、`BlogArticleViewDailyMapper`、`ArticleDailyCountStat` 和 `ArticleCategoryCountStat`。
- 公开文章详情读取在原有事务内同时更新文章累计阅读量和当日阅读量；任一写入失败会整体回滚。
- `BlogArticleMapper` 新增按日发布量聚合和分类内容数量聚合 SQL；分类统计只包含未删除文章，已删除或不存在的分类归入“未分类”。
- 新增 `DashboardTrendDayResp`、`DashboardCategoryDistributionResp`，`DashboardStatsResp` 返回最近 30 天连续趋势点和分类分布。
- `DashboardServiceImpl` 对最近 30 天日期补零，前端无需额外请求即可切换 7/30 天；阅读趋势来自每日聚合表，发布趋势来自文章发布时间。

前端新增/完善：

- 新增 `frontend/src/components/DashboardChart.vue`：封装 ECharts 初始化、响应式尺寸监听、选项更新和组件销毁，供后台统计图复用。
- `frontend/src/api/dashboard.ts`：补齐趋势点和分类分布类型。
- `AdminDashboardPage.vue`：新增近 7/30 天切换的“阅读折线 + 发布柱状”组合图，以及分类内容占比环形图和文字数值列表。
- 图表颜色读取现有主题变量，主题切换时重新生成配置；首次加载失败时展示不可用提示，不渲染空白图表。

### 19. 本轮继续完成：文章匿名点赞与评论层级回复

数据库新增/完善：

- 新增 `blog_article_like` 匿名点赞关系表，以 `article_id + visitor_key` 唯一键防止同一访客重复点赞。
- `database/upgrade_article_interaction.sql`：为已有数据库提供点赞关系表升级脚本；已有 `blog_article.like_count` 聚合值会保留。

后端新增/完善：

- 新增 `BlogArticleLike`、`BlogArticleLikeMapper`、`ArticleLikeStatusResp`、`ArticleLikeService`、`ArticleLikeServiceImpl` 和 `PublicArticleLikeController`。
- 新增公开点赞接口：
  - `GET /public/articles/{slug}/likes/status`
  - `POST /public/articles/{slug}/likes`
  - `DELETE /public/articles/{slug}/likes`
- 点赞接口通过 `X-Visitor-Key` 接收浏览器本地匿名标识；关系唯一键负责防重复，文章点赞总数使用公开/已发布/未删除状态条件更新，并与关系写入处于同一事务。
- 重复点赞不会重复增加计数，重复取消不会重复减少计数；点赞数条件自减限制 `like_count > 0`。
- 文章阅读量和点赞量更新显式保持 `update_time` 不变，避免访客互动被误认为文章内容编辑。
- 新增 `PublicCommentResp`，公开评论不再复用后台 `CommentResp`，不会返回访客邮箱、审核人和审核状态。
- 评论公开列表构建“一级评论 + 已通过回复”的两层结构；回复只允许指向同文章、已通过审核的一级评论。
- 回复提交事务会锁定父评论，避免父评论并发删除后生成孤儿回复；删除一级评论时同步逻辑删除其回复。
- 永久删除回收站文章时同步清理点赞关系，并补齐此前遗漏的每日阅读聚合清理。

前端新增/完善：

- 新增 `frontend/src/lib/visitor.ts`：生成并保存匿名访客标识；`localStorage` 不可用时使用当前页面内存标识。
- `frontend/src/api/publicArticle.ts`：新增点赞状态查询、点赞和取消点赞 API。
- `frontend/src/api/comment.ts`：拆分公开评论与后台评论 TypeScript 类型，公开类型不包含邮箱和审核字段。
- `ArticleDetailPage.vue`：新增点赞按钮、点赞总数、加载/提交/错误反馈；评论区展示两层回复，支持发起和取消回复。
- `AdminModerationPage.vue`：回复评论显示父评论ID，方便管理员识别审核上下文。

### 20. 本轮继续完成：管理员个人中心与密码修改

后端新增/完善：

- 新增 `ProfileUpdateReq`、`PasswordUpdateReq` 和 `AdminProfileController`。
- 新增后台个人中心接口：
  - `GET /admin/profile`
  - `PUT /admin/profile`
  - `PUT /admin/profile/password`
- `SysUserService`、`SysUserServiceImpl` 新增当前管理员资料更新和密码修改能力；资料更新只允许修改昵称、头像地址、邮箱和简介，不允许通过个人中心修改用户名、角色或账号状态。
- 密码修改会校验当前密码、两次新密码一致、新旧密码不同，以及新密码长度和字母/数字组合；密码继续使用项目现有 Hutool BCrypt 生成随机盐密文。
- 密码修改查询使用 `FOR UPDATE` 锁定当前管理员行，并在更新时附带原密码和启用状态条件，避免并发改密互相覆盖。
- 会话退出注册在数据库事务 `afterCommit` 阶段，只有密码修改事务提交成功后才退出当前会话。
- `OperationLogAspect` 在操作执行前保存登录用户ID，使退出登录、修改密码等会改变会话的操作仍能记录正确执行人；密码请求字段继续按现有规则脱敏。

前端新增/完善：

- 新增 `frontend/src/api/profile.ts`：个人资料读取、更新和修改密码 API。
- 新增 `AdminProfilePage.vue`：展示账号摘要和头像预览，支持维护昵称、头像地址、邮箱、简介以及修改登录密码。
- 资料更新成功后同步刷新全局 `appStore.user`，后台顶部昵称立即更新。
- 修改密码成功后清理本地 token，跳转登录页并展示使用新密码重新登录的提示。
- `router/index.ts`、`AdminLayout.vue`：新增 `/admin/profile` 路由、个人中心菜单和顶部昵称入口。
- `AdminLayout.vue`：修正父路由 `/admin` 在所有后台子页面同时高亮的问题，菜单只显示当前功能区选中状态。

### 21. 本轮继续完成：文章响应批量装配优化

后端完善：

- `ArticleServiceImpl` 新增内部 `ArticleRenderContext`，用于一次性装配一批文章所需的分类名称、专栏名称/访问标识、文章标签关系和标签名称。
- 后台文章分页、前台文章分页、公开搜索、热门文章、相关文章和归档文章列表均改为使用批量装配上下文。
- 移除响应装配中的逐篇 `resolveCategoryName`、`resolveColumn`、`listArticleTags` 查询路径，避免列表类接口每篇文章反复查分类、专栏和标签。
- 搜索响应仍保留原有摘要片段生成逻辑，接口字段不变；只把分类、专栏、标签名称解析改成批量上下文读取。
- 相关文章接口中保留候选标签查询，该查询用于相关度评分，不属于响应字段 N+1；最终返回的相关文章仍通过批量上下文装配响应。
- 单篇详情、新建、编辑、版本回滚返回 `ArticleResp` 时也复用同一装配方法，保持响应字段来源一致。

前端影响：

- 无需修改前端接口类型或页面逻辑，`ArticleListResp`、`ArticleSearchResp`、`ArticleResp` 字段保持兼容。

### 22. 本轮继续完成：公开互动轻量限流

后端新增/完善：

- 新增 `RateLimitProperties` 和 `RateLimitService`，使用内存固定窗口计数，适合单机演示环境拦截公开写接口刷请求。
- `application.yml` 新增 `technote.rate-limit` 配置：
  - `comment`：默认 60 秒 5 次。
  - `guestbook`：默认 60 秒 5 次。
  - `link-apply`：默认 3600 秒 3 次。
  - `article-like`：默认 60 秒 60 次。
- 限流 key 由 `规则名 + 客户端 IP` 组成；点赞额外拼接 `X-Visitor-Key`，降低同一网络下不同访客互相影响。
- 客户端 IP 优先读取 `X-Forwarded-For` 首个地址，其次读取 `X-Real-IP`，最后使用 `remoteAddr`；IP 字符串会做长度截断，避免异常头部撑大 key。
- 超限统一抛出 `BaseException(429, message)`，继续使用现有 `ApiResult` 返回结构。
- 定期清理过期计数器，避免长期运行后内存 map 无限制增长。

接入接口：

- `POST /public/comments`：评论提交限流。
- `POST /public/guestbooks`：留言提交限流。
- `POST /public/link-applies`：友链申请限流。
- `POST /public/articles/{slug}/likes`、`DELETE /public/articles/{slug}/likes`：点赞和取消点赞限流。
- 公开列表、评论列表、留言列表、点赞状态查询不做限流，避免影响正常浏览。

前端影响：

- 无需修改前端。超过限制时 Axios 会按现有拦截器抛出后端返回的错误消息，页面已有错误提示会展示。

### 23. 本轮继续完成：后台移动端导航与窄屏管理适配

前端完善：

- `AdminLayout.vue` 将桌面固定侧栏改为 `lg` 以上显示，移动端通过顶部菜单按钮打开抽屉导航。
- 移动端抽屉带遮罩层、关闭按钮和路由切换后自动关闭逻辑，避免进入后台页面后导航遮挡内容。
- 后台顶部栏增加小屏菜单入口，标题和当前管理员昵称都使用截断保护，减少长文本撑开头部。
- 主内容区改为 `min-w-0` + 小屏更紧凑内边距，横向滚动交给项目已有 `Table` 组件内部处理。
- 桌面侧栏增加 `h-screen` 和 `overflow-auto`，后台菜单较多时不会把整个页面高度撑乱。
- 顶部栏改为 sticky，在窄屏管理页面向下滚动时仍能快速打开导航或退出登录。

适配结论：

- 后台表格组件 `frontend/src/components/ui/table/Table.vue` 已经统一包裹 `w-full overflow-auto`，因此列表类管理页在窄屏下由表格自身横向滚动，不需要给整页增加横向滚动。
- 当前仅调整后台整体布局层，未改动文章、专栏、审核、仪表盘等具体业务页面的数据逻辑。

### 24. 本轮继续完成：归档热力图 SQL 聚合优化

后端完善：

- `BlogArticleMapper` 新增 `selectPublicDailyPublishCounts(startTime, endTime)`，由数据库按日期统计公开已发布文章数量。
- 热力图统计继续过滤 `deleted = 0`、`status = 1`、`visibility = 1`，不会把草稿、私密文章或已删除文章计入公开归档。
- SQL 使用 `COALESCE(publish_time, update_time, create_time)` 作为归档时间，保持和原 `resolveArchiveTime` 的兜底语义一致。
- `ArticleServiceImpl.getPublicArchiveHeatmap` 不再查询整年文章列表并在 Java 内存中分组，只负责年份校验、调用 Mapper、组装响应和计算全年总量。

接口影响：

- `GET /public/articles/archives/heatmap?year=2026` 路径、请求参数和响应结构不变，前端无需修改。

### 25. 本轮继续完成：公开提交验证码与敏感词过滤

后端新增/完善：

- 新增 `PublicSubmitGuardProperties`，通过 `technote.public-submit-guard` 配置公开提交防护开关、验证码 TTL、内存容量上限和敏感词列表。
- 新增 `PublicSubmitGuardService`，负责一次性数学验证码生成/校验、过期验证码清理和轻量敏感词过滤。
- 新增 `PublicCaptchaController`：`GET /public/captcha` 返回 `captchaId`、数学题目和有效期秒数。
- `CommentSubmitReq`、`GuestbookSubmitReq`、`LinkApplySubmitReq` 增加 `captchaId`、`captchaAnswer` 字段；字段是否必填由防护配置决定，DTO 只限制长度。
- `PublicCommentController`、`PublicGuestbookController`、`PublicLinkApplyController` 在进入业务 Service 前执行：限流 -> 验证码 -> 敏感词检查。
- 敏感词命中统一抛出 `BaseException(400, message)`，继续走现有 `ApiResult` 错误结构。

前端新增/完善：

- 新增 `frontend/src/api/guard.ts`，封装 `createCaptchaApi` 和验证码 payload 类型。
- 评论、留言和友链申请表单在页面挂载时加载验证码，提交时带上 `captchaId` 和 `captchaAnswer`。
- 提交成功或失败后刷新验证码，匹配后端一次性验证码语义，避免同一验证码被重复使用。
- 三个公开表单都保留原有加载/错误提示，并新增“换一题”按钮和验证码加载失败提示。

配置：

- `application.yml` 新增 `technote.public-submit-guard`：
  - `captcha.ttl-seconds`：默认 300 秒。
  - `captcha.max-entries`：默认 2000 条内存验证码。
  - `sensitive-word.words`：演示环境默认拦截博彩、贷款、发票、代刷、spam 等词。

### 26. 本轮继续完成：后台公开风控配置页

后端新增/完善：

- 新增 `blog_guard_setting` 表，用于持久化公开提交风控配置，避免把敏感词放进会公开返回的 `blog_setting`。
- 新增 `PublicSubmitGuardSetting`、`PublicSubmitGuardSettingMapper`、`PublicSubmitGuardSettingReq`、`PublicSubmitGuardSettingResp`。
- 新增 `AdminPublicSubmitGuardController`：
  - `GET /admin/public-submit-guard`
  - `PUT /admin/public-submit-guard`
- `PublicSubmitGuardService` 增加后台配置读写能力，并改为运行时优先读取数据库配置；数据库暂无记录时回退到 `application.yml` 默认配置。
- 新增 `database/upgrade_public_submit_guard.sql`，用于已有数据库升级；`database/technote_schema.sql` 已同步新表和初始化数据。

前端新增/完善：

- `frontend/src/api/guard.ts` 增加后台风控配置读取和保存 API。
- 新增 `AdminGuardSettingsPage.vue`，支持维护总开关、验证码开关、验证码 TTL、验证码内存保留上限、敏感词过滤开关、命中提示和敏感词列表。
- `router/index.ts` 增加 `/admin/public-submit-guard` 路由。
- `AdminLayout.vue` 增加“公开风控”菜单入口，移动端抽屉导航同步可访问。
- 公开评论、留言和友链申请表单补充验证码关闭场景：后端返回 `expireSeconds = 0` 时，前端不再强制填写验证码。

## 静态验证记录

已做：

- 对照数据库表 `blog_column`、`blog_column_article`、`blog_article.column_id` 与后端实体字段。
- 对照后端 Controller 路径与前端 API 路径。
- 对照 `columnId/columnName/columnSlug` 在后端响应、前端类型、后台页面、前台页面中的使用。
- 检查文章新建/编辑会同步专栏关系，避免只更新文章表或只更新关系表。
- 检查前台公开专栏文章查询仍复用公开文章过滤：`status = 1`、`visibility = 1`。
- 检查前端新增页面使用 Vue SFC、项目内 shadcn-vue 风格组件、`gap` 布局和响应式网格。
- 对照 `blog_article_auto_draft` 表与实体、请求、响应字段。
- 对照自动草稿 Controller 路径与前端 API：`/admin/article-auto-drafts`。
- 检查自动草稿独立于 `blog_article`，避免自动保存覆盖已发布文章。
- 检查正式保存后会调用草稿物理删除接口，减少旧草稿误恢复，并避免逻辑删除记录影响唯一键复用。
- 对照仪表盘统计字段 `autoDraftCount`、待审互动、最近文章、最近操作与前端展示字段。
- 检查新增前端页面未使用 shadcn 规则禁止的 `space-x-*`、`space-y-*`、`className`。
- 对照年度写作热力图接口路径 `/public/articles/archives/heatmap` 与前端 API。
- 检查热力图只统计公开已发布文章，不暴露私密或草稿内容。
- 检查归档页新增热力图使用项目内 shadcn-vue 风格组件和 `gap` 布局。
- 对照文章导出后端路径 `/admin/articles/{id}/export`、`/admin/articles/export` 与前端 API。
- 检查导出接口只允许后台登录用户访问，可导出公开/私密/草稿文章用于个人资产备份。
- 检查 Blob 下载响应已绕过 Axios `ApiResult` 拦截，避免 Markdown 文件被误判为失败响应。
- 检查文章管理页新增选择框、导出按钮仍使用项目内 shadcn-vue 风格组件和 `gap` 布局。
- 对照批量操作后端路径 `/admin/articles/batch/delete`、`/admin/articles/batch/visibility`、`/admin/articles/batch/category` 与前端 API。
- 检查批量删除复用回收站删除流程，不绕过删除快照和媒体引用清理。
- 检查批量分类允许空分类，表示将文章移出分类。
- 对照全文搜索后端路径 `/public/articles/search` 与前端 API、路由 `/search`。
- 检查全文搜索只返回公开已发布文章，不暴露私密或草稿内容。
- 检查搜索高亮由前端文本分段渲染，不使用 `v-html`。
- 检查顶部搜索框和搜索结果页使用项目内 shadcn-vue 风格组件和 `gap` 布局。
- 检查 Markdown 代码块仍先做 HTML 转义，代码高亮只包裹安全转义后的 token，不允许原始 HTML 注入。
- 检查代码复制按钮通过 `data-code-copy` 事件委托触发，公开详情页、后台预览、历史版本预览共用同一工具方法。
- 检查代码块样式已覆盖原有 `pre/code` 通用样式，避免行号、复制栏被文章正文样式挤坏。
- 对照自动草稿维护接口路径 `/admin/article-auto-drafts`、`/admin/article-auto-drafts/id/{id}`、`/admin/article-auto-drafts/cleanup` 与前端 API。
- 检查自动草稿维护删除仍使用物理删除，避免 `uk_auto_draft_key_user (draft_key, create_by, deleted)` 因逻辑删除记录影响复用。
- 检查自动草稿列表响应不返回完整 `content`，只返回 `contentPreview` 和 `contentLength`。
- 检查 `AdminAutoDraftsPage.vue` 未使用 `space-x-*`、`space-y-*`、`className` 和 `v-html`。
- 检查文章分页、公开分页、专栏文章分页和归档响应已切换为 `ArticleListResp`，不再返回完整 `content`。
- 检查文章详情、编辑、创建、更新、回滚接口仍返回 `ArticleResp`，不会影响编辑器和详情页正文渲染。
- 检查前端 `ArticleItem` 只保留在 `ArticleEditorPage.vue`、`ArticleDetailPage.vue` 等需要完整正文的场景，列表页使用 `ArticleListItem`。
- 检查 Markdown 标题提取和正文渲染共用 `createHeadingIdFactory`，目录链接与正文 `id` 保持一致。
- 检查标题提取会跳过代码块，避免代码块中的伪标题进入文章目录。
- 检查 `ArticleDetailPage.vue` 未使用 `space-x-*`、`space-y-*`、`className`，并且 `v-html` 仍只用于 Markdown 正文渲染。
- 检查首页、专栏、搜索、归档、友链、留言板和 404 页面均复用 `EmptyState.vue`，主次操作入口均指向现有公开路由。
- 检查公开页新增空状态未使用 `space-x-*`、`space-y-*`、`className` 和 `v-html`。
- 检查首页、专栏、搜索和留言板在接口加载失败时不会同时展示“加载失败”与“暂无内容”。
- 检查文章详情中的加载、评论等局部状态继续保留为局部提示，避免整页空状态组件破坏正文布局。
- 检查移动端公开主导航和分类/专栏/标签筛选入口均使用横向滚动容器，避免导航项被隐藏或挤压换行。
- 检查 `PublicPagination.vue` 在窄屏使用双列按钮、桌面端使用横向布局，首页、专栏和搜索页不再复制分页结构。
- 检查文章目录在移动端先于正文显示，在桌面端继续位于正文右侧并吸顶。
- 检查文章锚点移动端使用更大的 `scroll-margin-top`，避免标题被双层顶部导航遮挡。
- 检查公开页面新增移动适配代码未使用 `space-x-*`、`space-y-*`、`className` 和不必要的 `v-html`。
- 检查首页、专栏、文章详情、归档、友链、留言板和关于页的加载标记初始为 `true`，搜索页仅在 URL 已有关键词时初始进入加载态，避免 `onMounted` 前空状态闪烁。
- 检查公开异步页面的加载、错误和空状态条件互斥，接口失败时不会同时显示空状态。
- 检查评论和留言提交会先校验昵称与内容，提交期间禁用表单和按钮，并通过 `finally` 恢复提交状态。
- 检查文章正文与评论请求相互独立，评论失败仅在评论区域反馈，不会清空已成功加载的文章。
- 检查首页、搜索、专栏和文章详情使用请求序号忽略过期响应，避免快速切换条件后旧请求覆盖新页面数据。
- 检查 `LoadingState.vue` 和 `Alert.vue` 提供 `aria-live` 状态反馈，加载按钮图标使用 `data-icon="inline-start"`。
- 检查本轮公开页面反馈代码未使用 `space-x-*`、`space-y-*`、`className`，`v-html` 仍只用于经过现有 Markdown 安全渲染器处理的正文。
- 对照友链申请数据库字段、实体、请求、响应和服务映射，确认审核备注与状态字段一致。
- 对照公开提交和后台申请 Controller 路径与前端 API：`/public/link-applies`、`/admin/link-applies`。
- 检查审核通过使用事务、待审核状态条件更新和正式友链唯一约束，重复审核或并发创建失败时能够回滚。
- 检查公开申请与后台审核页面未使用 `space-x-*`、`space-y-*`、`className` 或 `v-html`。
- 对照热门文章与相关文章后端接口、前端 API 路径：`/public/articles/popular`、`/public/articles/{slug}/related`。
- 检查热门文章、相关文章和仪表盘热门排行都过滤 `status = PUBLISHED`、`visibility = PUBLIC`，不会暴露草稿或私密文章。
- 检查相关文章会排除当前文章，并按同专栏、同分类、共同标签相关度排序。
- 检查热门与相关文章数据库查询显式选择列表字段，不读取完整 Markdown 正文。
- 检查文章详情的正文、评论和相关文章异步状态相互独立，快速切换文章时继续使用请求序号忽略过期响应。
- 检查公开侧栏、文章详情推荐和仪表盘排行未使用 `space-x-*`、`space-y-*` 或 `className`。
- 对照 `blog_article_view_daily` 表、实体和 Mapper 字段，确认 `article_id/stat_date/view_count` 映射一致。
- 检查公开文章详情累计阅读量与每日阅读量在同一事务内写入，每日统计使用唯一键和 `ON DUPLICATE KEY UPDATE` 防止并发丢失。
- 检查趋势响应固定返回最近 30 天并补零，发布量按 `publish_time` 聚合，分类占比按数据库聚合而非逐分类查询。
- 对照仪表盘后端 `trendDays/categoryDistribution` 与前端 TypeScript 类型和图表消费字段。
- 检查 `DashboardChart.vue` 会监听容器尺寸、更新 ECharts 选项并在卸载时释放实例。
- 检查仪表盘趋势和分类占比页面未使用 `space-x-*`、`space-y-*`、`className`、手写 SVG 或 `v-html`。
- 对照 `blog_article_like` 表、实体、Mapper 和升级脚本字段，确认 `article_id/visitor_key/create_time` 映射一致，唯一键覆盖匿名点赞幂等边界。
- 对照点赞 Controller 与前端 API 路径和 `X-Visitor-Key` 请求头，确认状态查询、点赞、取消点赞契约一致。
- 检查点赞关系写入/删除与文章聚合计数更新处于同一事务，并使用公开文章状态条件和非负计数条件更新。
- 检查文章永久删除会清理点赞关系，文章软删除和恢复期间保留原点赞关系。
- 检查文章永久删除会清理每日阅读聚合，避免仪表盘继续统计已经永久删除的文章。
- 检查 `PublicCommentResp` 不包含 `email/status/auditBy/auditTime`，公开评论接口不再返回后台审核模型。
- 检查回复父评论必须属于同一文章、已通过审核且为一级评论，并在提交事务中锁定父评论。
- 检查删除一级评论时同步逻辑删除其回复，公开评论树不会展示孤儿或未审核回复。
- 检查文章详情点赞和层级回复交互未使用 `space-x-*`、`space-y-*`、`className` 或新增不安全 `v-html`。
- 对照 `sys_user` 的 `nickname/avatar_url/email/bio/password/status/update_by` 字段与个人中心请求、实体和更新逻辑。
- 对照个人中心 Controller 路径与前端 API：`/admin/profile`、`/admin/profile/password`。
- 检查个人资料接口不允许修改用户名、角色和账号状态，响应继续使用不含密码的 `UserInfoResp`。
- 检查密码修改会验证旧密码、确认密码、新旧密码差异，并使用 BCrypt 随机盐重新生成密码密文。
- 检查密码修改使用事务和 `FOR UPDATE` 行锁，退出会话只在事务提交成功后执行。
- 检查操作日志敏感字段规则包含 `password`，`oldPassword/newPassword/confirmPassword` 均不会明文落库。
- 检查退出和修改密码操作执行后，操作日志仍能使用操作前捕获的用户ID记录执行人。
- 检查个人中心页面、菜单和登录提示未使用 `space-x-*`、`space-y-*`、`className` 或 `v-html`。
- 检查后台菜单按当前路径显式判断激活状态，个人中心不会与数据概览同时高亮，文章编辑子路由仍归属文章管理。
- 检查文章后台分页、前台分页、热门文章、相关文章、归档列表和公开搜索均走 `ArticleRenderContext` 批量装配。
- 检查响应装配中不再保留逐篇查询分类、专栏和标签的旧方法。
- 检查公开搜索接口字段和前端类型未变，只优化分类、专栏、标签名称来源。
- 检查相关文章评分所需标签查询仍只用于相关度计算，最终响应装配使用批量上下文。
- 对照公开写接口 Controller，确认评论、留言、友链申请、点赞和取消点赞均在进入 Service 前执行限流。
- 检查公开读取接口没有接入限流，不影响文章、评论、留言和点赞状态正常浏览。
- 检查限流配置项 `technote.rate-limit` 与 `RateLimitProperties` 字段命名可按 Spring Boot 宽松绑定对应。
- 检查超限错误使用 `BaseException(429, ...)`，继续走统一 `ApiResult` 错误响应。
- 检查点赞限流 key 同时包含客户端 IP 和匿名访客标识，评论/留言/友链申请按客户端 IP 控制频率。
- 检查后台布局移动端导航只修改 `AdminLayout.vue`，没有改动后台业务接口和页面数据流。
- 检查后台表格统一由 `Table.vue` 提供内部横向滚动，`AdminLayout.vue` 主内容不再把整页撑出横向滚动条。
- 检查后台管理页搜索/筛选区主要使用响应式 `grid` 和 `gap` 布局，未发现新增 `space-x-*`、`space-y-*` 或 `className`。
- 对照热力图 Controller 路径、Service 方法和前端 API，确认本轮只改统计实现，不改变公开接口契约。
- 检查热力图 SQL 聚合继续使用公开文章条件：`deleted = 0`、`status = 1`、`visibility = 1`。
- 检查热力图 SQL 使用 `COALESCE(publish_time, update_time, create_time)`，与原 Java 归档时间兜底规则保持一致。
- 检查验证码接口 `GET /public/captcha` 与前端 `createCaptchaApi` 路径一致。
- 检查评论、留言、友链申请请求类型均扩展 `captchaId/captchaAnswer`，提交表单均在 payload 中带回验证码。
- 检查公开写 Controller 均保持先限流、再验证码/敏感词、最后进入业务 Service 的顺序。
- 检查敏感词过滤只接入公开写接口，不影响公开文章、评论列表、留言列表、友链列表等读取接口。
- 检查新增前端验证码 UI 未使用 `space-x-*`、`space-y-*` 或 `className`，按钮图标使用 `data-icon="inline-start"`。
- 检查后台公开风控配置不复用公开站点设置响应，避免敏感词通过 `/public/settings` 暴露。
- 检查后台风控配置接口使用 `@SaCheckLogin`，保存操作有 `OperationLog` 记录。
- 检查 `blog_guard_setting` 已同时写入主 schema 和升级脚本，已有库需要手动执行 `database/upgrade_public_submit_guard.sql`。
- 检查公开表单在验证码关闭时不会继续强制填写验证码。
- 检查当前目录不是 Git 仓库，无法使用 `git diff` 做最终差异审阅。

未做：

- 未运行 `npm install`、`npm run build`。
- 未运行 Maven 构建或测试。
- 未启动前端/后端服务。
- 未做浏览器截图和真实交互验证。

原因：当前项目 `AGENTS.md` 规定安装、构建、启动服务必须先单独询问；本轮按静态审查优先推进。

## 当前风险

- 未经过真实构建，可能仍存在编译期问题，需要后续允许后运行 `npm run build` 和 Maven 构建确认。
- 仪表盘统计接口目前实时查库，数据量很大后可对低频统计做短 TTL 缓存；待审数、自动草稿数这类后台即时数据暂不建议长缓存。
- 自动草稿已有后台维护页和手动过期清理接口，但还没有定时任务；如果希望无人值守清理，可后续增加低峰定时清理策略。
- 年度写作热力图已改为 Mapper SQL 按日期聚合；因为没有运行 Maven 构建，仍需后续通过编译确认 MyBatis 字段映射无误。
- 批量导出当前合并为一个 Markdown 文件；如果后续要求每篇独立文件，需要增加 zip 打包能力。
- 批量操作当前限制单次最多 100 篇；如果后续需要跨页全量批处理，应设计异步任务或按查询条件批处理接口。
- 全文搜索当前使用 MySQL `LIKE`，文章量较大后建议改为全文索引或独立搜索引擎。
- 专栏删除当前只允许删除无文章专栏，未接入回收站；如果后续希望恢复专栏，需要设计专栏快照和依赖检查。
- 评论、留言、友链申请和点赞已有单机内存限流；评论、留言和友链申请已增加数学验证码与敏感词过滤，并提供后台风控配置页。多实例部署时，验证码和限流仍需要改为 Redis 或网关级方案。
- 匿名点赞依赖浏览器本地标识，清理站点数据或更换浏览器后会视为新访客；它用于轻量互动幂等，不等同于账号级防刷。
- Markdown 渲染器是轻量实现，不是完整 CommonMark；代码高亮只覆盖常见关键字、字符串、数字和注释，不等同于完整语法高亮引擎。

## 下一段建议

建议继续做“构建验证和展示 polish”：

1. 在你允许后运行 `npm install`、`npm run build` 和 Maven 构建，确认类型和编译问题。
2. 启动前后端后做浏览器验证：登录后台、打开数据概览、编辑文章、触发自动草稿恢复与清理。
3. 给后台设置类页面补统一表单组件或字段校验样式，减少后续新增配置页时的重复表单代码。
4. 对后台移动端页面做真实浏览器截图验证，重点检查抽屉导航、表格横向滚动、编辑器侧栏和弹窗表单。
5. 按 `docs/DEMO_CHECKLIST.md` 做手动验收，并根据结果补充截图、录屏或修复任务。

### 27. 本轮继续完成：演示初始化数据脚本

新增：
- `database/seed_demo_data.sql`：可选演示数据脚本，覆盖分类、标签、专栏、公开文章、草稿、文章标签关系、专栏文章关系、每日阅读趋势、评论、留言、友链、友链申请和操作日志。

设计说明：
- 脚本只用于本地展示或验收演示，不会自动执行，也不会删除已有数据。
- 分类、标签、专栏和文章使用稳定 `slug` 做重复保护。
- 友链和友链申请使用 URL 与现有唯一约束语义对齐，避免重复插入。
- `blog_article_tag`、`blog_column_article` 使用唯一键配合 `INSERT IGNORE`，方便重复导入。
- `blog_article_view_daily` 使用 `(article_id, stat_date)` 唯一键和 `ON DUPLICATE KEY UPDATE`，重复导入时刷新演示阅读量。
- 评论、留言和操作日志使用内容片段做重复保护，避免重复执行后后台审核列表无限增长。

文档：
- `README.md` 新增“演示数据”说明，标明 `database/seed_demo_data.sql` 的用途、幂等策略和手动导入顺序。

### 28. 本轮继续完成：演示验收清单

新增：
- `docs/DEMO_CHECKLIST.md`：手动展示和交接验收清单，覆盖环境确认、数据库准备、构建启动、公开站点、后台管理、响应式适配、回归风险和验收结论记录。

设计说明：
- 清单明确区分“需要手动执行的步骤”和“当前已经验证的内容”，避免把未运行的 SQL、构建或服务启动误写成已通过。
- 数据库准备部分对齐 `database/technote_schema.sql`、升级脚本和 `database/seed_demo_data.sql`，方便全新库和已有库分别使用。
- 公开站点验收覆盖首页、文章详情、归档、搜索、专栏、友链、留言、关于页和 404。
- 后台验收覆盖登录、数据概览、文章管理、内容组织、互动审核、站点设置、公开风控、媒体库、回收站、日志和个人中心。
- 响应式验收单独列出桌面端、移动端和状态反馈，便于后续真实浏览器截图时逐项检查。

文档：
- `README.md` 目录结构新增 `database/seed_demo_data.sql` 和 `docs/DEMO_CHECKLIST.md`。
- `README.md` 新增“演示验收”说明，标明清单用途和当前未自动执行其中命令。

### 29. 本轮继续完成：后台设置类页面表单一致性

前端新增：
- `frontend/src/components/admin/AdminPageHeader.vue`：后台页面标题、说明和右侧操作区复用组件，统一设置类页面头部布局和长标题截断。
- `frontend/src/components/admin/AdminSettingsSection.vue`：后台设置区块卡片复用组件，统一 `CardHeader`、`CardTitle`、`CardDescription`、`CardContent` 和可选 `CardFooter` 结构。

前端改造：
- `frontend/src/views/admin/AdminSettingsPage.vue` 改为使用 `AdminPageHeader` 和 `AdminSettingsSection`，并拆分 `loading` 与 `saving`，加载时显示提示，保存时仅按钮显示加载图标。
- `frontend/src/views/admin/AdminGuardSettingsPage.vue` 改为使用同一套页面头部和设置区块组件，保留原有验证码、敏感词校验和保存逻辑。

设计说明：
- 本轮只抽取设置类页面的稳定布局，不改后端接口、不改表单字段、不改变公开风控业务逻辑。
- 新组件复用项目已有 shadcn-vue 风格 `Card`、`Button`、`Alert` 等组件，不新增依赖。
- 按钮图标继续使用 `data-icon="inline-start"`，布局使用 `gap`，未新增 `space-x-*`、`space-y-*` 或 `className`。
- 系统设置页加载态和保存态分离后，避免初次加载时误显示保存中的按钮状态。

### 30. 本轮继续完成：公开 RSS 与 sitemap

后端新增：
- `PublicFeedService`：公开订阅与站点地图服务接口。
- `PublicFeedServiceImpl`：生成 RSS 2.0 和 sitemap XML，复用公开文章分页列表和站点设置。
- `PublicFeedController`：新增 XML 输出接口：
  - `GET /rss.xml`
  - `GET /public/rss.xml`
  - `GET /public/feed/rss.xml`
  - `GET /sitemap.xml`
  - `GET /public/sitemap.xml`

配置：
- `application.yml` 新增 `technote.public-site-url`，默认 `http://localhost:5173`，用于生成 RSS 和 sitemap 里的前端公开链接。

设计说明：
- RSS 只取最近 20 篇公开文章，复用 `ArticleService.pagePublicArticles`，不会读取完整 Markdown 正文。
- sitemap 通过 `ArticleService.pagePublicArticles` 按 50 条翻页拿公开已发布文章，继续复用无正文列表 DTO，并补充首页、归档、专栏、友链、留言、关于页和专栏详情 URL。
- 文章 URL 使用前端路径 `/articles/{slug}`，专栏 URL 使用 `/columns/{slug}`，slug 通过 `UriUtils.encodePathSegment` 编码。
- XML 输出不走 `ApiResult`，直接返回 `ResponseEntity<String>` 和 XML content type，便于订阅器和搜索引擎读取。

文档：
- `README.md` 新增“公开订阅与站点地图”说明。
- `docs/DEMO_CHECKLIST.md` 增加 `technote.public-site-url`、RSS 和 sitemap 验收项。

### 31. 本轮继续完成：robots.txt 与公开订阅入口

后端完善：
- `PublicFeedService` 增加 `buildRobotsTxt()`。
- `PublicFeedServiceImpl` 增加 robots 文本生成，默认允许公开站点抓取，禁止 `/admin` 和 `/admin/`，并声明 `Sitemap: {technote.public-site-url}/sitemap.xml`。
- `PublicFeedController` 新增纯文本接口：
  - `GET /robots.txt`
  - `GET /public/robots.txt`

前端完善：
- `PublicLayout.vue` 右侧信息卡新增“订阅与索引”区块，提供 `/api/rss.xml`、`/api/sitemap.xml`、`/api/robots.txt` 三个入口。

设计说明：
- robots 与 RSS/sitemap 共用 `technote.public-site-url`，保证声明的 sitemap 是前端公开站点地址。
- 前端只是增加普通外链，不新增请求、不影响路由和公开数据加载。
- 因后端配置了 `/api` context-path，正式部署时若希望搜索引擎访问根路径 `/robots.txt`、`/sitemap.xml`，需要由前端静态服务或网关转发到后端对应接口。

文档：
- `README.md` 补充 `/api/robots.txt` 和部署转发说明。
- `docs/DEMO_CHECKLIST.md` 补充 robots 与公开侧栏订阅入口验收项。

### 32. 本轮继续完成：前端基础 SEO 元信息与 RSS 自动发现

前端新增：
- `frontend/src/lib/head.ts`：统一维护客户端 `<head>`，包括 `document.title`、description、keywords、Open Graph、Twitter summary、canonical 和 RSS alternate。

前端改造：
- `frontend/index.html` 增加默认 description 和 `link rel="alternate" type="application/rss+xml"`。
- `PublicLayout.vue` 接入站点设置，普通公开页面根据当前路由更新标题、描述、关键词和 canonical。
- `ArticleDetailPage.vue` 在加载中、加载成功和加载失败三种状态下更新文章详情页 head；加载成功后使用文章标题、摘要、标签和 slug。

设计说明：
- `head.ts` 保留站点默认值，站点设置加载后会重新应用当前 head，避免文章页先加载时站点标题仍停留在默认值。
- 普通公开页面由 `PublicLayout.vue` 管理，文章详情路由交给 `ArticleDetailPage.vue` 覆盖，避免 layout 在文章加载后覆盖文章标题。
- RSS alternate 指向 `/api/rss.xml`，与后端公开订阅接口保持一致。
- 这是 SPA 客户端基础 SEO，能改善浏览器标题、订阅发现和部分爬虫可读性，但不等同于 SSR/SSG。

文档：
- `README.md` 补充前端基础 head 元信息说明。
- `docs/DEMO_CHECKLIST.md` 增加首页、文章详情和 RSS alternate 的 head 验收项。

### 33. 本轮继续完成：文章详情分享与复制链接

前端新增：
- `frontend/src/lib/clipboard.ts`：通用文本复制工具，优先使用 `navigator.clipboard.writeText`，不支持时回退到临时 `textarea` + `document.execCommand('copy')`。

前端改造：
- `ArticleDetailPage.vue` 增加文章公开地址计算，基于当前站点 origin 和 `/articles/{slug}` 生成分享链接。
- 文章详情底部操作区新增“复制链接”和“分享”按钮。
- “分享”优先使用浏览器 Web Share API；浏览器不支持时自动复制文章链接。
- 复制和分享各自维护提交状态，避免按钮加载态互相干扰。

设计说明：
- 本轮只增强前端阅读体验，不改后端接口和文章数据结构。
- 复制/分享结果沿用页面内文字反馈，不引入新的 toast 依赖。
- 按钮图标继续使用 `data-icon="inline-start"`，布局沿用 `gap`，不新增 `space-x-*`、`space-y-*` 或 `className`。

文档：
- `docs/DEMO_CHECKLIST.md` 增加文章详情复制链接和 Web Share API 回退验收项。

### 34. 本轮继续完成：结构化数据 JSON-LD

前端完善：
- `frontend/src/lib/head.ts` 扩展 `setDocumentHead` 入参，支持写入或移除 `script#technote-json-ld[type="application/ld+json"]`。
- `PublicLayout.vue` 为普通公开页面输出 WebSite JSON-LD，并补充 SearchAction，搜索入口指向 `/search?keyword={search_term_string}`。
- `ArticleDetailPage.vue` 为文章详情成功加载状态输出 BlogPosting JSON-LD，包含 headline、description、url、datePublished、dateModified、author 和 keywords。
- 文章详情加载中和加载失败状态会清空结构化数据，避免从一篇文章切到另一篇文章时残留旧 BlogPosting。

设计说明：
- JSON-LD 仍由客户端运行时维护，和前一轮基础 head 元信息一样，属于 SPA 基础 SEO 增强，不等同于 SSR/SSG。
- 普通公开页面由 `PublicLayout.vue` 管理 WebSite 结构化数据；文章详情路由由 `ArticleDetailPage.vue` 覆盖为 BlogPosting，职责边界和原有 head 管理一致。
- `head.ts` 只负责 DOM head 写入，不引入业务接口依赖，保持工具函数低耦合。

文档：
- `README.md` 补充 JSON-LD、WebSite/SearchAction 和 BlogPosting 说明。
- `docs/DEMO_CHECKLIST.md` 增加公开页面、文章详情和路由切换后的 JSON-LD 验收项。

### 35. 本轮继续完成：构建前静态交接收口

静态核对：
- 核对 `frontend/src/api/*.ts` 与后端 Controller 的主要路径，确认文章、专栏、分类标签、评论/留言、友链、媒体、回收站、日志、仪表盘、公开风控、个人中心和登录接口路径与 `/api` context-path 对齐。
- 核对 `frontend/src/router/index.ts` 与 `AdminLayout.vue` 菜单，确认后台路由入口覆盖数据概览、文章管理、自动草稿、专栏、分类标签、友链、友链申请、媒体库、互动审核、公开风控、回收站、操作日志、个人中心和系统设置。
- 核对 `database` 目录与 `docs/DEMO_CHECKLIST.md`，确认全新库脚本和已有库升级脚本的职责边界。

文档修正：
- `README.md` 目录结构补齐 `upgrade_article_interaction.sql`、`upgrade_dashboard_statistics.sql`、`upgrade_link_apply.sql` 和 `upgrade_public_submit_guard.sql`。
- `README.md` 运行说明改为区分全新数据库执行 `technote_schema.sql`，已有数据库按需执行 4 个升级脚本，并补充 `upgrade_link_apply.sql` 执行前需要排查重复网址。

未改动：
- 本轮不改后端业务代码和前端页面逻辑，只修正交接文档中已有数据库升级步骤不完整的问题。

### 36. 本轮继续完成：后端新增模块源码风险扫描

静态核对：
- 核对 `blog_article_like`、`blog_article_view_daily`、`blog_link_apply`、`blog_guard_setting` 表定义与对应 Entity/Mapper/Service 字段，确认驼峰映射可覆盖 `article_id`、`visitor_key`、`stat_date`、`view_count`、`audit_remark`、`captcha_ttl_seconds`、`sensitive_words` 等字段。
- 确认当前项目没有 Mapper XML，新增 SQL 主要通过 Mapper 注解和 MyBatis-Plus 条件构造器完成；`TechNoteApplication` 的 `@MapperScan("com.technote.**.mapper")` 能覆盖新增 Mapper。
- 核对文章点赞流程：关系表使用 `INSERT IGNORE` + 唯一键防重，点赞/取消点赞在事务内配合文章表条件更新，更新条件包含 `deleted = 0`、`status = 1`、`visibility = 1`，取消点赞额外限制 `like_count > 0`。
- 核对每日阅读趋势：公开文章详情阅读成功后同时增加 `blog_article.view_count` 和 `blog_article_view_daily`；后台趋势按 `stat_date` 汇总阅读量，公开归档热力图按公开已发布文章聚合发布数量。
- 核对友链申请：提交时规范化 HTTP(S) URL、检查已有正式友链和待审核申请；审核时使用待审核状态条件更新，通过后在同一事务内创建默认隐藏的正式友链，并由唯一约束兜底并发重复。
- 核对公开风控：后台接口有 `@SaCheckLogin`，保存接口有操作日志；运行时优先读取 `blog_guard_setting`，没有数据库配置时回退 `application.yml` 默认值；公开写接口顺序为限流、验证码/敏感词检查、业务 Service。
- 核对生成列风险：`blog_link.active_site_url` 和 `blog_link_apply.pending_site_url` 是数据库生成列，未放入 Entity，避免插入/更新生成列导致 SQL 错误。

结论：
- 本轮未发现需要立即修改业务源码的字段映射、事务边界或接口路径问题。
- 仍未运行 Maven 编译/测试，后续需要在允许后通过构建验证注解 SQL、泛型 DTO 和 MyBatis 映射是否有编译期或运行期问题。

### 37. 本轮继续完成：前端源码风险扫描

静态核对：
- 核对 `frontend/src/router/index.ts` 与 `frontend/src/views` 页面清单，确认公开端首页、文章详情、专栏、归档、友链、留言、关于、搜索、404，以及后台数据概览、文章、自动草稿、专栏、分类标签、友链、友链申请、媒体、互动审核、公开风控、回收站、操作日志、个人中心、系统设置均有路由入口。
- 核对 `frontend/src/api/*.ts` 导出的接口类型和页面调用关系，重点检查文章、自动草稿、文章导出、点赞、评论、留言、友链申请、公开风控、个人资料、媒体上传和仪表盘统计字段。
- 核对公开写表单：评论、留言和友链申请都会加载验证码，提交 payload 会带 `captchaId/captchaAnswer`，并在 `expireSeconds = 0` 时允许跳过验证码。
- 核对后台公开风控页：表单字段与 `PublicSubmitGuardSettingPayload` 对齐，前端保存前校验验证码 TTL 和内存保留数量范围。
- 核对文章编辑器：正式保存使用 `ArticlePayload`，自动草稿使用 `ArticleAutoDraftPayload`，分类/专栏/状态/可见性/置顶通过 `Select` 事件转为数字，图片上传后使用媒体返回的 `filePath` 写入封面或 Markdown 正文。
- 核对 `v-html` 使用点：仅用于 Markdown 渲染预览、文章详情和关于页；`renderMarkdown` 会先转义原始 HTML，只允许 HTTP(S) 或站内相对链接/图片。
- 核对基础 UI 组件：`Input`、`Select`、`Checkbox`、`Button` 维持项目内 shadcn-vue 风格，`@` 路径别名在 `vite.config.ts` 和 `tsconfig.json` 中一致。

结论：
- 本轮未发现需要立即修改前端业务源码的 API 字段、表单 payload、路由入口或 Markdown 渲染风险。
- 仍未运行 `npm install`、`npm run build` 或浏览器验证；后续需要在允许后通过 `vue-tsc`/Vite 构建和真实页面交互验证模板、类型与响应式布局。

### 38. 本轮继续完成：最终构建前交接启动说明

新增文档：
- `docs/HANDOFF.md`：面向下一位开发者或 AI 的短交接说明，集中记录当前边界、环境版本、数据库脚本顺序、建议启动顺序、首轮验收路径、已静态核对内容、仍需重点验证事项和推荐下一步。

文档更新：
- `README.md` 增加 `docs/HANDOFF.md` 入口，方便先读短文档再进入完整验收清单。

设计说明：
- `HANDOFF.md` 不替代 `DEMO_CHECKLIST.md`，只解决“下一位接手者先看什么、先跑什么、哪些还没跑过”的交接问题。
- 文档明确保留当前限制：未运行 npm/Maven、未连接 MySQL、未启动服务、未做浏览器验收。

### 39. 本轮继续完成：最终完成度审计文档

新增文档：
- `docs/COMPLETION_AUDIT.md`：把原始目标拆成项目完整性、Vue 前端、shadcn 风格、Spring Boot 后端、MySQL 数据库、依赖构建、分轮交接和其它 AI 接手能力等可核对项，并逐项标注当前静态证据与状态。

文档更新：
- `README.md` 增加 `docs/COMPLETION_AUDIT.md` 入口，方便从项目总览直接进入最终完成度审计。

静态核对：
- 重新读取原始粘贴提示、`README.md`、`docs/PROGRESS.md` 和当前项目目录结构。
- 核对 `backend/pom.xml`：Spring Boot 3.3.7、Java 21、MyBatis-Plus、Sa-Token、Hutool、MySQL 驱动。
- 核对 `frontend/package.json`：Vue 3、Vite、TypeScript、Vue Router、Pinia、Tailwind、lucide-vue-next、ECharts，以及 `dev`/`build` 脚本。
- 核对后端源码目录：Controller、Entity、Mapper、Service、ServiceImpl、DTO、Enum、配置、日志、仪表盘、公开风控、RSS/sitemap/robots 等分层存在。
- 核对前端源码目录：公开页面、后台页面、布局、API、shadcn 风格 UI 组件、Markdown/head/clipboard/visitor 工具均存在。
- 核对数据库和文档目录：全量 schema、4 个升级脚本、演示数据、演示清单、快速交接、进度记录齐全。

设计说明：
- `COMPLETION_AUDIT.md` 只声明“静态满足”和“待运行验证”，避免把未运行的 Maven/npm/MySQL/浏览器验收误写成已通过。
- 当前 `rg` 在环境中被拒绝访问，本轮改用 `Get-ChildItem` 和 `Get-Content` 做只读静态取证。
- 本轮未修改业务源码，未执行安装、构建、启动服务或数据库命令。

### 40. 本轮继续完成：构建前配置语法和占位风险预检

静态核对：
- 复查 `backend/pom.xml` 中 `<description>`、`<dependencies>` 和 `</project>` 结构。此前普通 `Get-Content` 输出出现乱码，改用 UTF-8 读取后确认实际内容为 `<description>TechNote 技术博客后端服务</description>`，不是 XML 标签损坏。
- 复查 `backend/src/main/resources/application.yml` 中 `technote.public-submit-guard.sensitive-word` 配置。此前普通 `Get-Content` 输出看起来像 `message` 与 `words` 粘在一行，改用 UTF-8 读取后确认实际 YAML 换行正常，`message` 与 `words` 为独立字段。
- 扫描 `backend/src/main/java` 与 `frontend/src` 中的 `TODO`、`FIXME`、`mock`、`假数据`、`硬编码`、`未实现`、`Placeholder` 等风险词。未发现真实未完成功能；命中项主要是前端表单 `placeholder` 属性文本。
- 复查 `frontend/src/router/index.ts` 和 `frontend/src/layouts/AdminLayout.vue`，未发现 `AdminPlaceholderPage` 被路由或菜单引用。

结论：
- 本轮没有发现需要立即修改的构建阻断级配置错误。
- `AdminPlaceholderPage.vue` 当前是未引用文件，静态上不影响路由和页面入口；保留不删除，避免无关清理。
- 仍未运行 Maven、npm、MySQL 或浏览器验收，真正的强验证仍需在获得允许后执行。

### 41. 本轮继续完成：前后端导入路径一致性预检

静态核对：
- 扫描 `frontend/src` 下所有 `.ts` 和 `.vue` 文件的相对 `import`，按 `.ts`、`.vue`、`.js`、目录 `index.ts`、目录 `index.vue` 规则解析，结果为 `frontend-relative-imports-ok`。
- 扫描 `frontend/src` 下所有 `@/` 别名 `import`，按 `vite.config.ts` 和 `tsconfig.json` 中的 `@ -> src` 约定解析，结果为 `frontend-alias-imports-ok`。
- 扫描 `backend/src/main/java` 下所有 `com.technote.*` 内部 `import`，对照源码中的 package/class/interface/enum 声明，结果为 `backend-internal-imports-ok`。
- 扫描后端 Java 文件的 `package` 声明和目录路径是否一致，结果为 `backend-package-paths-ok`。

结论：
- 本轮没有发现前端缺失导入、后端内部 import 缺失或 Java package 路径不一致问题。
- 这只能证明源码路径层面的静态一致性，不能替代 `npm run build`、Maven 编译和真实启动验证。
- 本轮未修改业务源码，未执行安装、构建、启动服务或数据库命令。

### 42. 本轮继续完成：交接文档目录与轮次一致性收尾

文档修正：
- `README.md` 目录结构补充 `docs/HANDOFF.md` 和 `docs/COMPLETION_AUDIT.md`，避免目录树与当前 `docs` 实际文件不一致。
- `docs/COMPLETION_AUDIT.md` 将交接轮次从 1-38 同步为 1-41，并把自身状态从“当前新增”调整为“已有”，使审计文档与当前进度记录一致。

结论：
- 本轮仅修正文档描述，不修改前后端业务源码。
- 仍未运行 npm、Maven、MySQL 或浏览器验收。

### 43. 本轮继续完成：前后端接口路径一致性预检

静态核对：
- 抽取后端 `*Controller.java` 中的 `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping` 和 `@PatchMapping`，形成方法 + 路径集合。
- 抽取前端 `frontend/src/api/*.ts` 中 `request.get/post/put/delete/patch` 的请求路径，统一按 `request.ts` 的 `baseURL: '/api'` 去对齐后端 `server.servlet.context-path: /api`。
- 将前端模板参数 `${...}` 和后端路径参数 `{id}`、`{slug}` 等统一归一为 `{var}` 后比对。
- 初次脚本因 `{id}` 与 `{var}` 归一化不完整误报 `GET /admin/articles/{var}/export`；复核后确认后端存在 `GET /admin/articles/{id}/export`，修正脚本后结果为 `frontend-api-paths-match-backend-controllers`。
- 额外用 UTF-8 读取 `AuthController.java` 并扫描所有 `@OperationLog` 注解，确认此前普通控制台输出中的乱码不是 Java 字符串损坏，结果为 `operation-log-annotations-look-ok`。

结论：
- 本轮没有发现前端 API 调用路径与后端 Controller 映射不一致的问题。
- 本轮仍属于静态源码预检，不能替代真实服务启动后的接口联调、认证状态校验和请求参数校验。
- 本轮未修改业务源码，未执行安装、构建、启动服务或数据库命令。

### 44. 本轮继续完成：后端 Entity 与数据库字段映射预检

静态核对：
- 抽取 `database/technote_schema.sql` 中 20 张全量建表表结构，按普通列名解析字段，排除 `PRIMARY`、`UNIQUE`、`KEY`、`FULLTEXT`、`CONSTRAINT`、`INDEX` 等索引定义。
- 抽取后端 `entity`、`log/entity` 和公开风控实体中的 `@TableName`、`@TableId`、普通字段、`@TableField` 显式字段，按 MyBatis-Plus 下划线映射规则将 Java 字段转换为数据库列名。
- 初次脚本按反引号列名解析 schema，但当前 SQL 列名没有反引号，导致全量误报；修正解析规则后结果为 `entity-columns-match-schema`。
- 复查 4 个升级脚本新增内容：
  - `blog_article_like` 对应 `BlogArticleLike`。
  - `blog_article_view_daily` 对应 `BlogArticleViewDaily`。
  - `blog_link_apply.audit_remark` 对应 `BlogLinkApply.auditRemark`。
  - `blog_guard_setting` 对应 `PublicSubmitGuardSetting`。
- `upgrade_link_apply.sql` 中 `blog_link.active_site_url` 和 `blog_link_apply.pending_site_url` 是数据库生成列，未放入 `BlogLink`、`BlogLinkApply` Entity，避免插入/更新生成列导致 SQL 错误。

结论：
- 当前全量 schema 中 Entity 映射所需字段静态对齐，未发现缺失列或错误表名。
- 本轮没有修改业务源码。
- 仍未连接 MySQL，SQL 真实执行和 MyBatis-Plus 运行时映射仍需在获得允许后验证。

### 45. 本轮继续完成：仓库忽略规则交接记录与目录树同步

修复内容：
- 上一轮已新增并推送根目录 `.gitignore`，忽略 `frontend/node_modules/`、`frontend/dist/`、`backend/target/`、上传目录、日志、本地 IDE 配置和本地环境变量文件，避免后续安装依赖或构建后误提交大文件与本地配置。
- 更新 `README.md` 目录结构，补充 `.gitignore`，保持项目总览与当前根目录一致。

验证：
- `git status --short` 确认本轮仅修改 `README.md` 和 `docs/PROGRESS.md`。
- `.gitignore` 已在提交 `chore: add project gitignore` 中推送到远程 `origin/main`。
- 本轮未运行 npm、Maven、MySQL 或浏览器验收。

### 46. 本轮继续完成：后端本地配置支持环境变量覆盖

修复内容：
- `backend/src/main/resources/application.yml` 将 MySQL 连接地址、用户名、密码改为 `${MYSQL_URL:...}`、`${MYSQL_USERNAME:root}`、`${MYSQL_PASSWORD:root}`，保留本地默认值，同时支持部署环境覆盖。
- `technote.public-site-url` 改为 `${TECHNOTE_PUBLIC_SITE_URL:http://localhost:5173}`，避免部署后必须直接改源码配置。
- 上传根目录和访问前缀改为 `${TECHNOTE_UPLOAD_ROOT_PATH:uploads}`、`${TECHNOTE_UPLOAD_ACCESS_PREFIX:/uploads}`。
- `README.md` 补充可用环境变量说明。
- `docs/HANDOFF.md` 同步数据库、公开站点和上传目录的环境变量接手说明。

设计说明：
- 保留默认值是为了不破坏本机演示启动方式。
- 通过环境变量覆盖部署配置，可以避免把真实数据库账号密码写入远程仓库。
- 本轮未运行后端启动或 Maven 构建，后续仍需在获得允许后验证 Spring Boot 占位符解析和数据库连接。

### 47. 本轮继续完成：前台学习路线与题库入口页面

新增内容：
- `frontend/src/views/public/RoadmapPage.vue`：新增原创学习路线/面试题页面，包含橙色视觉头图区、技术方向筛选、图标技术卡片、面试题训练入口和四阶段学习路线。
- `frontend/src/router/index.ts`：新增 `/roadmap` 前台路由。
- `frontend/src/layouts/PublicLayout.vue`：主导航新增“题库”，并在题库页切换为全宽内容布局，避免被博客左右侧栏挤压导致卡片显示不全。
- `README.md`：补充学习路线与题库入口说明。

设计说明：
- 本轮未复制参考网站图片和图标资源；图标使用项目已有 `lucide-vue-next`，并通过卡片色彩、背景、标签和进度条形成独立视觉风格。
- 当前页面先采用前端内置数据，点击卡片跳转到现有搜索页，后续如果要做数据库题库模块，可以把页面数组替换为接口返回，页面结构不需要推翻。
- 题库页单独使用全宽公共布局，普通首页、专栏、归档、友链、留言等页面仍保留原三栏博客布局。

验证：
- 已静态核对路由、导航入口、页面模板结构和 README/PROGRESS 记录。
- 未运行 `npm install`、`npm run build`、开发服务器或浏览器验收，因为当前 AGENTS 规则要求这类命令必须先单独询问。

### 48. 本轮继续完成：公开留言响应隐私边界修复

修复内容：
- 新增 `PublicGuestbookResp`，公开留言响应只包含 `id`、`nickname`、`content`、`replyContent`、`createTime`。
- `GuestbookService` 和 `GuestbookServiceImpl` 将公开留言列表、公开留言提交返回类型改为 `PublicGuestbookResp`，后台分页审核继续使用 `GuestbookResp`。
- `PublicGuestbookController` 不再返回 `GuestbookResp`，避免公开接口带出 `email`、`status`、`auditBy`、`auditTime`、`updateTime` 等后台字段。
- `frontend/src/api/comment.ts` 新增 `PublicGuestbookItem`，公开留言 API 使用公开类型；后台审核页继续使用 `GuestbookItem`。
- `GuestbookPage.vue` 改用 `PublicGuestbookItem`，前台页面不再依赖后台审核字段。
- `README.md` 同步说明公开评论和留言都不会返回访客邮箱、审核状态等后台字段。

设计说明：
- 复用评论模块已有的公开/后台 DTO 分离思路，Controller 只暴露公开响应，ServiceImpl 内部通过单独转换方法控制字段边界。
- 本轮不改数据库结构，不影响后台审核列表、审核操作和删除操作。
- 提交留言仍保留入库邮箱字段供后台审核查看，但公开提交成功响应不会回传邮箱。

验证：
- 已静态核对公开 Controller、Service 接口、ServiceImpl 转换方法、前端 API 类型和公开留言页面引用。
- 未运行 Maven、npm 构建、后端服务或浏览器验收，因为当前 AGENTS 规则要求这类命令必须先单独询问。

### 49. 本轮继续完成：公开友链申请响应隐私边界修复

修复内容：
- 新增 `PublicLinkApplyResp`，公开友链申请响应只包含 `id`、`siteName`、`siteUrl`、`iconUrl`、`description`、`createTime`。
- `LinkApplyService` 和 `LinkApplyServiceImpl` 将公开友链申请提交返回类型改为 `PublicLinkApplyResp`，后台分页审核继续使用 `LinkApplyResp`。
- `PublicLinkApplyController` 不再返回后台 `LinkApplyResp`，避免公开接口带出 `applicantEmail`、`status`、`auditRemark`、`auditBy`、`auditTime`、`updateTime` 等字段。
- `frontend/src/api/link.ts` 新增 `PublicLinkApplyItem`，公开提交 API 使用公开类型；后台友链申请审核页继续使用 `LinkApplyItem`。
- `README.md` 同步说明公开评论、留言和友链申请都不会返回访客邮箱、审核状态等后台字段。

设计说明：
- 保留 `LinkApplyResp` 给后台审核列表使用，公开提交只回传展示和确认所需的基础站点信息。
- 申请邮箱仍会正常入库，供后台审核联系申请人，但公开提交成功响应不再回传邮箱和审核信息。
- 本轮不改数据库结构，不影响友链申请审核、通过后创建隐藏友链和删除申请的原有流程。

验证：
- 已静态核对公开 Controller、Service 接口、ServiceImpl 转换方法、前端 API 类型和后台审核页类型引用。
- 未运行 Maven、npm 构建、后端服务或浏览器验收，因为当前 AGENTS 规则要求这类命令必须先单独询问。

### 50. 本轮继续完成：公开友链列表响应边界修复

修复内容：
- 新增 `PublicLinkResp`，公开友链列表只返回 `id`、`siteName`、`siteUrl`、`iconUrl`、`description`。
- `PublicLinkController` 和 `LinkService.listVisibleLinks` 改为返回 `PublicLinkResp`，避免 `/public/links` 暴露 `sortOrder`、`status`、`createTime`、`updateTime` 等后台管理字段。
- `LinkServiceImpl` 新增 `toPublicLinkResp`，后台友链管理继续使用 `LinkResp`，公开列表和后台管理的响应边界分开维护。
- `frontend/src/api/link.ts` 新增 `PublicLinkItem`，公开友链页改用公开类型；后台友链管理页继续使用 `LinkItem`。
- `README.md` 同步说明公开友链列表也不会返回后台显示状态等内部字段。

设计说明：
- 公开列表仍然按 `VISIBLE` 状态过滤并按后台排序字段排序，但排序值本身不再返回给访客端。
- 本轮不改数据库结构，不影响友链新增、编辑、隐藏、删除和友链申请审核流程。
- DTO 拆分后，后续如果公开页需要新增展示字段，可以只扩展 `PublicLinkResp`，不会把后台管理字段顺带暴露出去。

验证：
- 已静态核对公开 Controller、Service 接口、ServiceImpl 转换方法、前端 API 类型和公开/后台友链页面引用。
- 未运行 Maven、npm 构建、后端服务或浏览器验收，因为当前 AGENTS 规则要求这类命令必须先单独询问。

### 51. 本轮继续完成：公开分类标签专栏响应与计数口径修复

修复内容：
- 新增 `PublicCategoryResp`、`PublicTagResp`、`PublicColumnResp`，公开分类、标签、专栏接口不再返回 `status`、`sortOrder`、`createTime`、`updateTime` 等后台管理字段。
- `BlogCategoryMapper`、`BlogTagMapper` 新增公开文章计数 SQL，只统计 `deleted = 0`、`status = 1`、`visibility = 1` 的文章。
- `TaxonomyService.listVisibleCategories`、`TaxonomyService.listTags` 和 `ColumnService` 的公开查询方法改为返回公开 DTO；后台管理分页、新增、编辑仍使用原有 DTO。
- `ColumnServiceImpl` 将后台专栏响应和前台专栏响应拆成独立转换方法，公开专栏继续使用已有 `selectPublicArticleCount`。
- 前端 `taxonomy.ts`、`column.ts` 新增公开类型，`PublicLayout.vue` 和 `ColumnsPage.vue` 改用公开类型。
- `README.md` 同步说明公开分类、标签、专栏的文章数量只统计公开已发布文章。

设计说明：
- 后台仍可看到全部未删除文章数量，方便内容维护；前台只展示访客真实可访问的文章数量，避免私密或草稿数量被侧面暴露。
- 本轮不改变分类、标签、专栏表结构，也不改变公开文章分页逻辑，只收紧公开响应字段和计数口径。

验证：
- 已静态核对公开 Controller、Service 接口、ServiceImpl 转换方法、Mapper SQL、前端 API 类型和公开布局/专栏页引用。
- 未运行 Maven、npm 构建、后端服务或浏览器验收，因为当前 AGENTS 规则要求这类命令必须先单独询问。

### 52. 本轮继续完成：公开站点设置响应边界修复

修复内容：
- 新增 `PublicSettingResp`，公开站点设置只返回前台布局和关于页需要展示的站点标题、描述、作者信息、公告和关于内容等字段。
- `SettingService` 新增 `getPublicSetting()`，后台 `getSetting()` 和 `saveSetting()` 继续返回 `SettingResp`，保留记录 ID、创建时间和更新时间供后台使用。
- `PublicSettingController` 改为返回 `PublicSettingResp`，避免 `/public/settings` 暴露内部记录 ID 和维护时间。
- 前端 `setting.ts` 新增 `PublicSettingItem`，公开布局 `PublicLayout.vue` 和关于页 `AboutPage.vue` 改用公开类型；后台设置页继续使用原 `SettingPayload`。
- `README.md` 同步说明公开设置接口不返回记录 ID 和创建/更新时间。

设计说明：
- 站点标题、SEO 描述、作者介绍、公告和关于内容属于前台展示数据；记录主键和维护时间只服务后台管理，不应作为公开接口契约。
- 本轮不改数据库结构，不影响后台保存站点设置。

验证：
- 已静态核对公开 Controller、Service 接口、ServiceImpl 转换方法、前端 API 类型和公开页面引用。
- 未运行 Maven、npm 构建、后端服务或浏览器验收，因为当前 AGENTS 规则要求这类命令必须先单独询问。
