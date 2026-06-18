<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { LoaderCircle, Search, SunMoon } from 'lucide-vue-next'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import type { ArticleListItem } from '../api/article'
import { listPublicColumnsApi, type ColumnItem } from '../api/column'
import { listPopularPublicArticlesApi } from '../api/publicArticle'
import { getPublicSettingApi, type SettingItem } from '../api/setting'
import { listPublicCategoriesApi, listPublicTagsApi, type CategoryItem, type TagItem } from '../api/taxonomy'
import { Badge } from '../components/ui/badge'
import { Button } from '../components/ui/button'
import { Card, CardContent } from '../components/ui/card'
import { Input } from '../components/ui/input'
import { configureSiteHeadDefaults, setDocumentHead } from '../lib/head'
import { useAppStore } from '../stores/appStore'

const appStore = useAppStore()
const route = useRoute()
const router = useRouter()
const categories = ref<CategoryItem[]>([])
const columns = ref<ColumnItem[]>([])
const tags = ref<TagItem[]>([])
const popularArticles = ref<ArticleListItem[]>([])
const popularLoading = ref(true)
const searchKeyword = ref(String(route.query.keyword || ''))
const searchInputRef = ref<InstanceType<typeof Input>>()
const navItems = [
  { label: '首页', to: '/' },
  { label: '专栏', to: '/columns' },
  { label: '归档', to: '/archives' },
  { label: '友链', to: '/links' },
  { label: '留言', to: '/guestbook' },
  { label: '关于', to: '/about' },
]
const setting = ref<SettingItem>({
  siteTitle: 'TechNote',
  siteDescription: '面向技术创作者的个人内容管理与分享系统',
  authorName: 'TechNote Admin',
  authorProfile: '专注技术沉淀、项目复盘和工程实践记录。',
})

const routeTitleMap: Record<string, string> = {
  '/': '',
  '/columns': '专栏',
  '/archives': '归档',
  '/links': '友链',
  '/guestbook': '留言',
  '/about': '关于',
  '/search': '搜索',
}

const toggleTheme = () => {
  appStore.setTheme(appStore.theme === 'dark' ? 'light' : 'dark')
}

const submitSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    return
  }
  await router.push({ path: '/search', query: { keyword } })
}

const focusSearch = () => {
  const input = searchInputRef.value?.$el as HTMLInputElement | undefined
  input?.focus()
}

const handleShortcut = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    focusSearch()
  }
}

const buildPublicPageTitle = () => {
  if (route.path.startsWith('/articles/')) {
    return undefined
  }
  if (route.path.startsWith('/columns/')) {
    return '专栏'
  }
  if (route.path === '/search' && route.query.keyword) {
    return `搜索：${String(route.query.keyword)}`
  }
  return routeTitleMap[route.path]
}

const updatePublicHead = () => {
  if (route.path.startsWith('/articles/')) {
    return
  }
  const siteName = setting.value.siteTitle || 'TechNote'
  const siteDescription = setting.value.siteDescription || setting.value.authorProfile
  const publicOrigin = typeof window === 'undefined' ? '' : window.location.origin
  setDocumentHead({
    title: buildPublicPageTitle(),
    description: siteDescription,
    keywords: setting.value.siteKeywords,
    canonicalPath: route.fullPath,
    structuredData: {
      '@context': 'https://schema.org',
      '@type': 'WebSite',
      name: siteName,
      description: siteDescription,
      url: publicOrigin,
      potentialAction: {
        '@type': 'SearchAction',
        target: `${publicOrigin}/search?keyword={search_term_string}`,
        'query-input': 'required name=search_term_string',
      },
    },
  })
}

onMounted(async () => {
  window.addEventListener('keydown', handleShortcut)
  updatePublicHead()
  void loadPopularArticles()
  try {
    const [siteSetting, categoryList, columnList, tagList] = await Promise.all([
      getPublicSettingApi(),
      listPublicCategoriesApi(),
      listPublicColumnsApi(),
      listPublicTagsApi(),
    ])
    setting.value = siteSetting
    configureSiteHeadDefaults({
      siteTitle: siteSetting.siteTitle,
      description: siteSetting.siteDescription || siteSetting.authorProfile,
      keywords: siteSetting.siteKeywords,
    })
    updatePublicHead()
    categories.value = categoryList
    columns.value = columnList
    tags.value = tagList
  } catch {
    categories.value = []
    columns.value = []
    tags.value = []
  }
})

const loadPopularArticles = async () => {
  popularLoading.value = true
  try {
    popularArticles.value = await listPopularPublicArticlesApi(5)
  } catch {
    popularArticles.value = []
  } finally {
    popularLoading.value = false
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleShortcut)
})

watch(
  () => route.query.keyword,
  () => {
    if (route.path === '/search') {
      searchKeyword.value = String(route.query.keyword || '')
    }
  },
)

watch(
  () => route.fullPath,
  () => {
    updatePublicHead()
  },
)
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <header class="sticky top-0 z-20 border-b bg-background/95 backdrop-blur">
      <div class="mx-auto flex h-14 max-w-7xl items-center gap-2 px-3 sm:gap-4 sm:px-4">
        <RouterLink to="/" class="max-w-24 shrink-0 truncate text-base font-semibold sm:max-w-40" :title="setting.siteTitle">
          {{ setting.siteTitle }}
        </RouterLink>
        <form class="relative min-w-0 flex-1" @submit.prevent="submitSearch">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" :size="16" />
          <Input
            ref="searchInputRef"
            v-model="searchKeyword"
            class="bg-surface pl-9 pr-3 sm:pr-16"
            placeholder="搜索公开文章"
          />
          <kbd class="pointer-events-none absolute right-2 top-1/2 hidden -translate-y-1/2 rounded border bg-background px-1.5 py-0.5 text-[11px] text-muted-foreground lg:block">
            Ctrl K
          </kbd>
        </form>
        <nav class="hidden items-center gap-4 text-sm text-muted-foreground lg:flex" aria-label="主要导航">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="transition-colors hover:text-foreground"
            active-class="text-foreground"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
        <Button variant="ghost" size="icon" aria-label="切换主题" @click="toggleTheme">
          <SunMoon />
        </Button>
      </div>
      <nav class="border-t lg:hidden" aria-label="移动端主要导航">
        <div class="mx-auto flex max-w-7xl gap-1 overflow-x-auto px-3 py-2 sm:px-4">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="shrink-0 rounded-md px-2.5 py-1.5 text-sm text-muted-foreground transition-colors hover:bg-hover hover:text-foreground"
            active-class="bg-hover text-foreground"
          >
            {{ item.label }}
          </RouterLink>
        </div>
      </nav>
    </header>

    <main class="mx-auto grid max-w-7xl grid-cols-1 gap-4 px-3 py-4 sm:px-4 lg:grid-cols-[220px_1fr_260px]">
      <nav
        v-if="categories.length > 0 || columns.length > 0 || tags.length > 0"
        class="flex gap-2 overflow-x-auto pb-1 lg:hidden"
        aria-label="内容筛选"
      >
        <RouterLink
          to="/"
          class="shrink-0 whitespace-nowrap rounded-md border bg-background px-2.5 py-1.5 text-xs hover:bg-hover"
        >
          全部文章
        </RouterLink>
        <RouterLink
          v-for="item in categories"
          :key="`category-${item.id}`"
          :to="{ path: '/', query: { categoryId: item.id } }"
          class="shrink-0 whitespace-nowrap rounded-md border bg-background px-2.5 py-1.5 text-xs hover:bg-hover"
        >
          分类 · {{ item.name }}
        </RouterLink>
        <RouterLink
          v-for="item in columns"
          :key="`column-${item.id}`"
          :to="`/columns/${item.slug}`"
          class="shrink-0 whitespace-nowrap rounded-md border bg-background px-2.5 py-1.5 text-xs hover:bg-hover"
        >
          专栏 · {{ item.name }}
        </RouterLink>
        <RouterLink
          v-for="item in tags"
          :key="`tag-${item.id}`"
          :to="{ path: '/', query: { tagId: item.id } }"
          class="shrink-0 whitespace-nowrap rounded-md border bg-background px-2.5 py-1.5 text-xs hover:bg-hover"
        >
          # {{ item.name }}
        </RouterLink>
      </nav>

      <Card class="hidden lg:block">
        <CardContent class="flex flex-col gap-4 pt-4">
          <div>
            <div class="mb-2 text-sm font-medium">分类</div>
            <div class="flex flex-col gap-2 text-sm text-muted-foreground">
              <RouterLink
                v-for="item in categories"
                :key="item.id"
                :to="{ path: '/', query: { categoryId: item.id } }"
                class="truncate hover:text-foreground"
                :title="item.name"
              >
                {{ item.name }}
              </RouterLink>
              <span v-if="categories.length === 0">暂无分类</span>
            </div>
          </div>

          <div>
            <div class="mb-2 text-sm font-medium">专栏</div>
            <div class="flex flex-col gap-2 text-sm text-muted-foreground">
              <RouterLink
                v-for="item in columns"
                :key="item.id"
                :to="`/columns/${item.slug}`"
                class="truncate hover:text-foreground"
                :title="item.name"
              >
                {{ item.name }}
              </RouterLink>
              <span v-if="columns.length === 0">暂无专栏</span>
            </div>
          </div>

          <div>
            <div class="mb-2 text-sm font-medium">标签</div>
            <div class="flex flex-wrap gap-2">
              <RouterLink v-for="item in tags" :key="item.id" :to="{ path: '/', query: { tagId: item.id } }">
                <Badge variant="outline">{{ item.name }}</Badge>
              </RouterLink>
              <span v-if="tags.length === 0" class="text-sm text-muted-foreground">暂无标签</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <section class="min-w-0">
        <RouterView />
      </section>

      <Card class="h-fit">
        <CardContent class="flex flex-col gap-3 pt-4">
          <div class="break-words text-sm font-medium">{{ setting.authorName || setting.siteTitle }}</div>
          <p class="break-words text-sm leading-6 text-muted-foreground">{{ setting.authorProfile || setting.siteDescription }}</p>
          <div v-if="setting.announcement" class="break-words rounded-md border bg-background p-3 text-sm leading-6 text-muted-foreground">
            {{ setting.announcement }}
          </div>
          <section class="flex flex-col gap-2 border-t pt-3">
            <div class="text-sm font-medium">热门文章</div>
            <div v-if="popularLoading" role="status" class="flex items-center gap-2 text-sm text-muted-foreground">
              <LoaderCircle class="animate-spin" />
              正在加载热门排行
            </div>
            <RouterLink
              v-for="(item, index) in popularArticles"
              :key="item.id"
              :to="`/articles/${item.slug}`"
              class="group flex items-start gap-2 rounded-md px-1 py-1.5 hover:bg-hover"
            >
              <Badge variant="outline" class="mt-0.5 shrink-0">{{ index + 1 }}</Badge>
              <span class="min-w-0">
                <span class="line-clamp-2 break-words text-sm leading-5 group-hover:text-primary">{{ item.title }}</span>
                <span class="mt-1 block text-xs text-muted-foreground">{{ item.viewCount || 0 }} 阅读</span>
              </span>
            </RouterLink>
            <span v-if="!popularLoading && popularArticles.length === 0" class="text-sm text-muted-foreground">
              暂无热门文章
            </span>
          </section>
          <section class="flex flex-col gap-2 border-t pt-3">
            <div class="text-sm font-medium">订阅与索引</div>
            <div class="flex flex-col gap-2 text-sm text-muted-foreground">
              <a href="/api/rss.xml" target="_blank" rel="noopener noreferrer" class="hover:text-foreground">
                RSS 订阅
              </a>
              <a href="/api/sitemap.xml" target="_blank" rel="noopener noreferrer" class="hover:text-foreground">
                sitemap.xml
              </a>
              <a href="/api/robots.txt" target="_blank" rel="noopener noreferrer" class="hover:text-foreground">
                robots.txt
              </a>
            </div>
          </section>
        </CardContent>
      </Card>
    </main>
  </div>
</template>
