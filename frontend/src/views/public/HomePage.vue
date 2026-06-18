<script setup lang="ts">
import { LoaderCircle, Search } from 'lucide-vue-next'
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { pagePublicArticlesApi } from '../../api/publicArticle'
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
const articles = ref<ArticleListItem[]>([])
const keyword = ref(String(route.query.keyword || ''))
const pageSize = 10
const total = ref(0)
const loading = ref(true)
const error = ref('')
let articleLoadRequestId = 0

const parsePageNo = () => {
  const parsed = Number(route.query.pageNo || 1)
  return Number.isNaN(parsed) || parsed < 1 ? 1 : parsed
}

const pageNo = ref(parsePageNo())
const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))
const hasFilters = computed(() =>
  Boolean(keyword.value || route.query.categoryId || route.query.columnId || route.query.tagId),
)
const emptyTitle = computed(() => (hasFilters.value ? '没有找到匹配文章' : '暂无公开文章'))
const emptyDescription = computed(() =>
  hasFilters.value ? '当前筛选条件下没有公开文章，可以清空筛选后重新浏览。' : '站点还没有发布公开文章，可以先去留言板打个招呼。',
)

const queryNumber = (name: string) => {
  const value = route.query[name]
  if (!value) return undefined
  const parsed = Number(value)
  return Number.isNaN(parsed) ? undefined : parsed
}

const loadArticles = async () => {
  const requestId = ++articleLoadRequestId
  loading.value = true
  error.value = ''
  articles.value = []
  total.value = 0
  try {
    const page = await pagePublicArticlesApi({
      pageNo: pageNo.value,
      pageSize,
      keyword: keyword.value,
      categoryId: queryNumber('categoryId'),
      columnId: queryNumber('columnId'),
      tagId: queryNumber('tagId'),
    })
    if (requestId !== articleLoadRequestId) return
    articles.value = page.records
    total.value = page.total
  } catch (err) {
    if (requestId !== articleLoadRequestId) return
    error.value = err instanceof Error ? err.message : '文章加载失败'
  } finally {
    if (requestId === articleLoadRequestId) {
      loading.value = false
    }
  }
}

const syncQuery = async () => {
  await router.replace({
    path: '/',
    query: {
      ...route.query,
      keyword: keyword.value || undefined,
      pageNo: pageNo.value > 1 ? pageNo.value : undefined,
    },
  })
}

const search = async () => {
  pageNo.value = 1
  await syncQuery()
}

const clearFilters = async () => {
  keyword.value = ''
  pageNo.value = 1
  await router.replace({ path: '/' })
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await syncQuery()
}

watch(
  () => route.query,
  () => {
    keyword.value = String(route.query.keyword || '')
    pageNo.value = parsePageNo()
    loadArticles()
  },
)

onMounted(loadArticles)
</script>

<template>
  <div class="flex flex-col gap-3">
    <Card>
      <CardContent class="grid gap-2 p-3 sm:grid-cols-[1fr_auto_auto]">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" :size="16" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索标题、摘要或访问标识" @keyup.enter="search" />
        </div>
        <Button class="min-w-20" :disabled="loading" @click="search">
          <LoaderCircle v-if="loading" class="animate-spin" data-icon="inline-start" />
          {{ loading ? '加载中' : '搜索' }}
        </Button>
        <Button variant="ghost" :disabled="loading" @click="clearFilters">清空</Button>
      </CardContent>
    </Card>

    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <LoadingState
      v-if="loading && articles.length === 0"
      title="正在加载公开文章"
      description="正在读取文章列表和当前筛选结果。"
    />

    <Card v-for="article in articles" :key="article.id" class="bg-background transition-colors hover:bg-hover">
      <CardHeader>
        <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <Badge v-if="article.topFlag === 1">置顶</Badge>
          <span>{{ article.categoryName || '未分类' }}</span>
          <span v-if="article.columnName">专栏：{{ article.columnName }}</span>
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
      :title="emptyTitle"
      :description="emptyDescription"
      :action-label="hasFilters ? '清空筛选' : '去留言'"
      :action-to="hasFilters ? '/' : '/guestbook'"
      secondary-label="查看归档"
      secondary-to="/archives"
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
  </div>
</template>
