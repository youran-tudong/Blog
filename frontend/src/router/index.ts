import { createRouter, createWebHistory } from 'vue-router'
import AdminDashboardPage from '../views/admin/AdminDashboardPage.vue'
import AdminAutoDraftsPage from '../views/admin/AdminAutoDraftsPage.vue'
import AdminGuardSettingsPage from '../views/admin/AdminGuardSettingsPage.vue'
import AdminLoginPage from '../views/admin/AdminLoginPage.vue'
import AdminArticlesPage from '../views/admin/AdminArticlesPage.vue'
import AdminColumnsPage from '../views/admin/AdminColumnsPage.vue'
import AdminSettingsPage from '../views/admin/AdminSettingsPage.vue'
import AdminTaxonomyPage from '../views/admin/AdminTaxonomyPage.vue'
import ArticleEditorPage from '../views/admin/ArticleEditorPage.vue'
import AboutPage from '../views/public/AboutPage.vue'
import ArchivesPage from '../views/public/ArchivesPage.vue'
import ArticleDetailPage from '../views/public/ArticleDetailPage.vue'
import ColumnsPage from '../views/public/ColumnsPage.vue'
import GuestbookPage from '../views/public/GuestbookPage.vue'
import HomePage from '../views/public/HomePage.vue'
import LinksPage from '../views/public/LinksPage.vue'
import NotFoundPage from '../views/public/NotFoundPage.vue'
import RoadmapPage from '../views/public/RoadmapPage.vue'
import SearchPage from '../views/public/SearchPage.vue'
import AdminLinksPage from '../views/admin/AdminLinksPage.vue'
import AdminLinkApplicationsPage from '../views/admin/AdminLinkApplicationsPage.vue'
import AdminMediaPage from '../views/admin/AdminMediaPage.vue'
import AdminModerationPage from '../views/admin/AdminModerationPage.vue'
import AdminOperationLogsPage from '../views/admin/AdminOperationLogsPage.vue'
import AdminProfilePage from '../views/admin/AdminProfilePage.vue'
import AdminRecyclePage from '../views/admin/AdminRecyclePage.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import PublicLayout from '../layouts/PublicLayout.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: PublicLayout,
      children: [
        { path: '', component: HomePage },
        { path: 'articles/:slug', component: ArticleDetailPage },
        { path: 'columns', component: ColumnsPage },
        { path: 'columns/:slug', component: ColumnsPage },
        { path: 'archives', component: ArchivesPage },
        { path: 'roadmap', component: RoadmapPage },
        { path: 'links', component: LinksPage },
        { path: 'guestbook', component: GuestbookPage },
        { path: 'about', component: AboutPage },
        { path: 'search', component: SearchPage },
        { path: ':pathMatch(.*)*', component: NotFoundPage },
      ],
    },
    {
      path: '/admin/login',
      component: AdminLoginPage,
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        { path: '', component: AdminDashboardPage },
        { path: 'articles', component: AdminArticlesPage },
        { path: 'articles/new', component: ArticleEditorPage },
        { path: 'articles/:id/edit', component: ArticleEditorPage },
        { path: 'auto-drafts', component: AdminAutoDraftsPage },
        { path: 'columns', component: AdminColumnsPage },
        { path: 'taxonomy', component: AdminTaxonomyPage },
        { path: 'links', component: AdminLinksPage },
        { path: 'link-applications', component: AdminLinkApplicationsPage },
        { path: 'media', component: AdminMediaPage },
        { path: 'moderation', component: AdminModerationPage },
        { path: 'public-submit-guard', component: AdminGuardSettingsPage },
        { path: 'recycle', component: AdminRecyclePage },
        { path: 'operation-logs', component: AdminOperationLogsPage },
        { path: 'profile', component: AdminProfilePage },
        { path: 'settings', component: AdminSettingsPage },
      ],
    },
  ],
})
