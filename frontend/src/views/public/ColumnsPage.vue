<script setup lang="ts">
import { LoaderCircle, Search } from 'lucide-vue-next'
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import {
  getPublicColumnApi,
  listPublicColumnsApi,
  pagePublicColumnArticlesApi,
  type ColumnItem,
} from '../../api/column'
import type { ArticleListItem } from '../../api/article'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PublicPagination from '../../components/PublicPagination.vue'

const route = useRoute()
const router = useRouter()
const columns = ref<ColumnItem[]>([])
const currentColumn = ref<ColumnItem>()
const articles = ref<ArticleListItem[]>([])
const keyword = ref(String(route.query.keyword || ''))
const pageSize = 10
const total = ref(0)
const loading = ref(true)
const error = ref('')
let pageLoadRequestId = 0

const currentSlug = computed(() => {
  const value = route.params.slug
  return Array.isArray(value) ? value[0] : value
})

const parsePageNo = () => {
  const parsed = Number(route.query.pageNo || 1)
  return Number.isNaN(parsed) || parsed < 1 ? 1 : parsed
}

const pageNo = ref(parsePageNo())
const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))
const articleEmptyActionLabel = computed(() => (keyword.value ? '清空搜索' : '浏览专栏列表'))
const articleEmptyActionTo = computed(() => (keyword.value && currentSlug.value ? `/columns/${currentSlug.value}` : '/columns'))

const loadPage = async () => {
  const requestId = ++pageLoadRequestId
  loading.value = true
  error.value = ''
  const requestedSlug = currentSlug.value
  const requestedPageNo = pageNo.value
  const requestedKeyword = keyword.value
  if (requestedSlug) {
    articles.value = []
    total.value = 0
    if (currentColumn.value?.slug !== requestedSlug) {
      currentColumn.value = undefined
    }
  }
  try {
    const columnList = await listPublicColumnsApi()
    if (requestId !== pageLoadRequestId) return
    columns.value = columnList
    if (!requestedSlug) {
      currentColumn.value = undefined
      articles.value = []
      total.value = 0
      return
    }
    const loadedColumn = await getPublicColumnApi(requestedSlug)
    if (requestId !== pageLoadRequestId) return
    currentColumn.value = loadedColumn
    const page = await pagePublicColumnArticlesApi(requestedSlug, {
      pageNo: requestedPageNo,
      pageSize,
      keyword: requestedKeyword,
    })
    if (requestId !== pageLoadRequestId) return
    articles.value = page.records
    total.value = page.total
  } catch (err) {
    if (requestId !== pageLoadRequestId) return
    error.value = err instanceof Error ? err.message : '专栏加载失败'
  } finally {
    if (requestId === pageLoadRequestId) {
      loading.value = false
    }
  }
}

const syncQuery = async () => {
  if (!currentSlug.value) {
    return
  }
  await router.replace({
    path: `/columns/${currentSlug.value}`,
    query: {
      keyword: keyword.value || undefined,
      pageNo: pageNo.value > 1 ? pageNo.value : undefined,
    },
  })
}

const search = async () => {
  pageNo.value = 1
  await syncQuery()
}

const clearSearch = async () => {
  if (!currentSlug.value) {
    return
  }
  keyword.value = ''
  pageNo.value = 1
  await router.replace({ path: `/columns/${currentSlug.value}` })
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await syncQuery()
}

watch(
  () => route.fullPath,
  () => {
    keyword.value = String(route.query.keyword || '')
    pageNo.value = parsePageNo()
    loadPage()
  },
)

onMounted(loadPage)
</script>

<template>
  <div class="flex flex-col gap-4">
    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <template v-if="!currentSlug">
      <div>
        <h1 class="text-xl font-semibold">专栏</h1>
        <p class="mt-1 text-sm text-muted-foreground">按主题聚合的系列文章，适合连续阅读和项目复盘。</p>
      </div>

      <div class="grid gap-4 md:grid-cols-2">
        <RouterLink v-for="item in columns" :key="item.id" :to="`/columns/${item.slug}`">
          <Card class="h-full overflow-hidden bg-background transition-colors hover:bg-hover">
            <div v-if="item.coverUrl" class="aspect-video overflow-hidden border-b">
              <img :src="item.coverUrl" :alt="item.name" class="size-full object-cover" />
            </div>
            <CardHeader>
              <div class="flex items-start justify-between gap-3">
                <CardTitle class="min-w-0 break-words text-lg">{{ item.name }}</CardTitle>
                <Badge class="shrink-0" variant="outline">{{ item.articleCount }} 篇</Badge>
              </div>
            </CardHeader>
            <CardContent>
              <p class="line-clamp-3 text-sm leading-6 text-muted-foreground">
                {{ item.description || '这个专栏还没有填写简介。' }}
              </p>
            </CardContent>
          </Card>
        </RouterLink>
      </div>

      <LoadingState
        v-if="loading && columns.length === 0"
        title="正在加载公开专栏"
        description="正在整理可阅读的专栏列表。"
      />

      <EmptyState
        v-if="!loading && !error && columns.length === 0"
        title="暂无公开专栏"
        description="当前还没有可见专栏，可以先浏览首页文章或稍后再来。"
        action-label="返回首页"
        action-to="/"
      />
    </template>

    <template v-else>
      <LoadingState
        v-if="loading && !currentColumn"
        title="正在加载专栏"
        description="正在读取专栏信息和文章列表。"
      />

      <Card v-if="currentColumn" class="overflow-hidden">
        <div v-if="currentColumn?.coverUrl" class="aspect-[5/2] overflow-hidden border-b">
          <img :src="currentColumn.coverUrl" :alt="currentColumn.name" class="size-full object-cover" />
        </div>
        <CardHeader>
          <div class="flex flex-wrap items-start justify-between gap-3">
            <div class="min-w-0">
              <CardTitle class="break-words text-2xl">{{ currentColumn?.name || '专栏' }}</CardTitle>
              <p class="mt-2 max-w-2xl break-words text-sm leading-6 text-muted-foreground">
                {{ currentColumn?.description || '这个专栏还没有填写简介。' }}
              </p>
            </div>
            <Badge variant="outline">{{ currentColumn?.articleCount || 0 }} 篇文章</Badge>
          </div>
        </CardHeader>
        <CardContent class="grid gap-2 sm:grid-cols-[1fr_auto_auto]">
          <div class="relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" :size="16" />
            <Input v-model="keyword" class="pl-9" placeholder="在此专栏内搜索文章" @keyup.enter="search" />
          </div>
          <Button class="min-w-20" :disabled="loading" @click="search">
            <LoaderCircle v-if="loading" class="animate-spin" data-icon="inline-start" />
            {{ loading ? '加载中' : '搜索' }}
          </Button>
          <Button variant="ghost" :disabled="loading" @click="clearSearch">清空</Button>
        </CardContent>
      </Card>

      <LoadingState
        v-if="loading && currentColumn && articles.length === 0"
        title="正在加载专栏文章"
        description="正在读取当前专栏的公开文章。"
      />

      <Card v-for="article in articles" :key="article.id" class="bg-background transition-colors hover:bg-hover">
        <CardHeader>
          <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <Badge v-if="article.topFlag === 1">置顶</Badge>
            <span>{{ article.categoryName || '未分类' }}</span>
            <span>{{ article.publishTime || article.createTime }}</span>
            <span>{{ article.viewCount }} 阅读</span>
          </div>
          <CardTitle class="text-xl">
            <RouterLink :to="`/articles/${article.slug}`" class="break-words hover:text-primary">
              {{ article.title }}
            </RouterLink>
          </CardTitle>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <p class="break-words text-sm leading-6 text-muted-foreground">
            {{ article.summary || '作者还没有填写摘要，点击标题阅读全文。' }}
          </p>
          <div class="flex flex-wrap gap-2">
            <Badge v-for="tag in article.tagNames" :key="tag" variant="secondary">{{ tag }}</Badge>
          </div>
        </CardContent>
      </Card>

      <EmptyState
        v-if="!loading && !error && articles.length === 0"
        title="这个专栏暂无公开文章"
        description="当前专栏还没有匹配的公开文章，可以清空搜索或浏览其他专栏。"
        :action-label="articleEmptyActionLabel"
        :action-to="articleEmptyActionTo"
        secondary-label="返回首页"
        secondary-to="/"
      />

      <PublicPagination
        v-if="!error && total > 0"
        :page-no="pageNo"
        :total-pages="totalPages()"
        :total="total"
        :loading="loading"
        @previous="goPage(pageNo - 1)"
        @next="goPage(pageNo + 1)"
      />
    </template>
  </div>
</template>
