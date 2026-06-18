<script setup lang="ts">
import {
  ArchiveRestore,
  BarChart3,
  BookOpen,
  ClipboardCheck,
  FileText,
  Images,
  Link2,
  Menu,
  MessageSquareText,
  ScrollText,
  Settings,
  ShieldCheck,
  Tags,
  UserRound,
  X,
} from 'lucide-vue-next'
import { onMounted, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { getCurrentUserApi, logoutApi } from '../api/auth'
import { Button } from '../components/ui/button'
import { useAppStore } from '../stores/appStore'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const checking = ref(true)
const mobileMenuOpen = ref(false)

const menus = [
  { label: '数据概览', path: '/admin', icon: BarChart3 },
  { label: '文章管理', path: '/admin/articles', icon: FileText },
  { label: '自动草稿', path: '/admin/auto-drafts', icon: FileText },
  { label: '专栏管理', path: '/admin/columns', icon: BookOpen },
  { label: '分类标签', path: '/admin/taxonomy', icon: Tags },
  { label: '友链管理', path: '/admin/links', icon: Link2 },
  { label: '友链申请', path: '/admin/link-applications', icon: ClipboardCheck },
  { label: '媒体库', path: '/admin/media', icon: Images },
  { label: '互动审核', path: '/admin/moderation', icon: MessageSquareText },
  { label: '公开风控', path: '/admin/public-submit-guard', icon: ShieldCheck },
  { label: '回收站', path: '/admin/recycle', icon: ArchiveRestore },
  { label: '操作日志', path: '/admin/operation-logs', icon: ScrollText },
  { label: '个人中心', path: '/admin/profile', icon: UserRound },
  { label: '系统设置', path: '/admin/settings', icon: Settings },
]

onMounted(async () => {
  if (!appStore.token) {
    await router.replace('/admin/login')
    return
  }
  try {
    appStore.setUser(await getCurrentUserApi())
  } catch {
    appStore.clearAuth()
    await router.replace('/admin/login')
  } finally {
    checking.value = false
  }
})

const handleLogout = async () => {
  try {
    await logoutApi()
  } finally {
    appStore.clearAuth()
    await router.replace('/admin/login')
  }
}

const menuItemClass = (path: string) => {
  const active = path === '/admin' ? route.path === '/admin' : route.path === path || route.path.startsWith(`${path}/`)
  return [
    'flex items-center gap-2 rounded-md border-l-[3px] px-3 py-2 text-sm',
    active ? 'border-l-primary bg-hover text-foreground' : 'border-transparent text-muted-foreground hover:bg-hover',
  ]
}

watch(
  () => route.path,
  () => {
    mobileMenuOpen.value = false
  },
)
</script>

<template>
  <div v-if="checking" class="flex min-h-screen items-center justify-center bg-background text-muted-foreground">
    正在校验登录状态...
  </div>
  <div v-else class="flex min-h-screen bg-background text-foreground">
    <aside class="sticky top-0 hidden h-screen w-60 shrink-0 overflow-auto border-r bg-surface lg:block">
      <div class="flex h-14 items-center border-b px-4 text-base font-semibold">TechNote Admin</div>
      <nav class="flex flex-col gap-1 p-3">
        <RouterLink
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          :class="menuItemClass(item.path)"
        >
          <component :is="item.icon" :size="16" />
          <span class="truncate">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>
    <div
      v-if="mobileMenuOpen"
      class="fixed inset-0 z-40 bg-black/35 lg:hidden"
      role="presentation"
      @click="mobileMenuOpen = false"
    />
    <aside
      class="fixed inset-y-0 left-0 z-50 flex w-[min(18rem,86vw)] flex-col border-r bg-surface shadow-lg transition-transform lg:hidden"
      :class="mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'"
      aria-label="后台移动导航"
    >
      <div class="flex h-14 items-center justify-between border-b px-4">
        <span class="text-base font-semibold">TechNote Admin</span>
        <Button variant="ghost" size="icon" aria-label="关闭菜单" @click="mobileMenuOpen = false">
          <X />
        </Button>
      </div>
      <nav class="flex flex-1 flex-col gap-1 overflow-auto p-3">
        <RouterLink
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          :class="menuItemClass(item.path)"
        >
          <component :is="item.icon" :size="16" />
          <span class="truncate">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>
    <div class="flex min-w-0 flex-1 flex-col">
      <header class="sticky top-0 z-30 flex h-14 items-center justify-between gap-3 border-b bg-background px-3 sm:px-5">
        <div class="flex min-w-0 items-center gap-2">
          <Button variant="ghost" size="icon" class="lg:hidden" aria-label="打开菜单" @click="mobileMenuOpen = true">
            <Menu />
          </Button>
          <span class="truncate text-sm text-muted-foreground">后台管理</span>
        </div>
        <div class="flex items-center gap-3">
          <RouterLink to="/admin/profile" class="max-w-28 truncate text-sm hover:text-primary sm:max-w-none">
            {{ appStore.user?.nickname || '管理员' }}
          </RouterLink>
          <Button variant="outline" @click="handleLogout">退出</Button>
        </div>
      </header>
      <main class="min-w-0 flex-1 overflow-x-hidden p-3 sm:p-5">
        <RouterView />
      </main>
    </div>
  </div>
</template>
