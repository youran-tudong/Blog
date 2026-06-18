<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { LoaderCircle, Search } from 'lucide-vue-next'
import { searchPublicArticlesApi, type ArticleSearchItem } from '../../api/publicArticle'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PublicPagination from '../../components/PublicPagination.vue'

interface HighlightSegment {
  text: string
  hit: boolean
}

const route = useRoute()
const router = useRouter()
const results = ref<ArticleSearchItem[]>([])
const keyword = ref(String(route.query.keyword || ''))
const sort = ref<'latest' | 'views'>(route.query.sort === 'views' ? 'views' : 'latest')
const pageSize = 10
const total = ref(0)
const loading = ref(Boolean(String(route.query.keyword || '').trim()))
const error = ref('')
let searchRequestId = 0

const parsePageNo = () => {
  const parsed = Number(route.query.pageNo || 1)
  return Number.isNaN(parsed) || parsed < 1 ? 1 : parsed
}

const pageNo = ref(parsePageNo())
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
const currentKeyword = computed(() => String(route.query.keyword || '').trim())

const splitHighlight = (text?: string): HighlightSegment[] => {
  const source = text || ''
  const key = currentKeyword.value
  if (!source || !key) {
    return [{ text: source, hit: false }]
  }
  const lowerSource = source.toLowerCase()
  const lowerKey = key.toLowerCase()
  const segments: HighlightSegment[] = []
  let cursor = 0
  let index = lowerSource.indexOf(lowerKey)
  while (index >= 0) {
    if (index > cursor) {
      segments.push({ text: source.slice(cursor, index), hit: false })
    }
    segments.push({ text: source.slice(index, index + key.length), hit: true })
    cursor = index + key.length
    index = lowerSource.indexOf(lowerKey, cursor)
  }
  if (cursor < source.length) {
    segments.push({ text: source.slice(cursor), hit: false })
  }
  return segments.length > 0 ? segments : [{ text: source, hit: false }]
}

const loadResults = async () => {
  const requestId = ++searchRequestId
  if (!currentKeyword.value) {
    results.value = []
    total.value = 0
    loading.value = false
    error.value = ''
    return
  }
  loading.value = true
  error.value = ''
  results.value = []
  total.value = 0
  try {
    const page = await searchPublicArticlesApi({
      pageNo: pageNo.value,
      pageSize,
      keyword: currentKeyword.value,
      sort: sort.value,
    })
    if (requestId !== searchRequestId) return
    results.value = page.records
    total.value = page.total
  } catch (err) {
    if (requestId !== searchRequestId) return
    error.value = err instanceof Error ? err.message : '搜索失败'
  } finally {
    if (requestId === searchRequestId) {
      loading.value = false
    }
  }
}

const syncQuery = async () => {
  await router.replace({
    path: '/search',
    query: {
      keyword: keyword.value.trim() || undefined,
      sort: sort.value === 'views' ? sort.value : undefined,
      pageNo: pageNo.value > 1 ? pageNo.value : undefined,
    },
  })
}

const search = async () => {
  pageNo.value = 1
  await syncQuery()
}

const changeSort = async (value: string | number) => {
  sort.value = value === 'views' ? 'views' : 'latest'
  pageNo.value = 1
  await syncQuery()
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await syncQuery()
}

watch(
  () => route.query,
  () => {
    keyword.value = String(route.query.keyword || '')
    sort.value = route.query.sort === 'views' ? 'views' : 'latest'
    pageNo.value = parsePageNo()
    loadResults()
  },
)

onMounted(loadResults)
</script>

<template>
  <div class="flex flex-col gap-4">
    <Card>
      <CardHeader>
        <CardTitle>搜索</CardTitle>
        <p class="text-sm text-muted-foreground">检索公开文章的标题、摘要、正文和访问标识。</p>
      </CardHeader>
      <CardContent class="grid gap-2 md:grid-cols-[1fr_140px_auto]">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" :size="16" />
          <Input v-model="keyword" class="pl-9" placeholder="输入关键词后回车" @keyup.enter="search" />
        </div>
        <Select :model-value="sort" @update:model-value="changeSort">
          <option value="latest">按时间排序</option>
          <option value="views">按阅读排序</option>
        </Select>
        <Button class="min-w-20" :disabled="loading" @click="search">
          <LoaderCircle v-if="loading" class="animate-spin" data-icon="inline-start" />
          {{ loading ? '搜索中' : '搜索' }}
        </Button>
      </CardContent>
    </Card>

    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <div class="break-words text-sm text-muted-foreground">
      <template v-if="loading">正在搜索“{{ currentKeyword }}”...</template>
      <template v-else-if="currentKeyword">“{{ currentKeyword }}” 共找到 {{ total }} 条公开结果</template>
      <template v-else>输入关键词开始搜索</template>
    </div>

    <LoadingState
      v-if="loading && results.length === 0"
      title="正在搜索公开文章"
      description="正在检索标题、摘要和正文内容。"
    />

    <Card v-for="item in results" :key="item.id" class="bg-background transition-colors hover:bg-hover">
      <CardHeader>
        <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <span>{{ item.categoryName || '未分类' }}</span>
          <span v-if="item.columnName">专栏：{{ item.columnName }}</span>
          <span>{{ item.publishTime || item.createTime || '-' }}</span>
          <span>{{ item.viewCount }} 阅读</span>
        </div>
        <CardTitle class="text-xl">
          <RouterLink :to="`/articles/${item.slug}`" class="break-words hover:text-primary">
            <template v-for="(segment, index) in splitHighlight(item.title)" :key="`${item.id}-title-${index}`">
              <mark v-if="segment.hit" class="rounded-sm bg-primary/20 px-0.5 text-foreground">{{ segment.text }}</mark>
              <span v-else>{{ segment.text }}</span>
            </template>
          </RouterLink>
        </CardTitle>
      </CardHeader>
      <CardContent class="flex flex-col gap-3">
        <p class="break-words text-sm leading-6 text-muted-foreground">
          <template v-for="(segment, index) in splitHighlight(item.snippet || item.summary)" :key="`${item.id}-snippet-${index}`">
            <mark v-if="segment.hit" class="rounded-sm bg-primary/20 px-0.5 text-foreground">{{ segment.text }}</mark>
            <span v-else>{{ segment.text }}</span>
          </template>
        </p>
        <div class="flex flex-wrap gap-2">
          <Badge v-for="tag in item.tagNames" :key="tag" variant="secondary">{{ tag }}</Badge>
        </div>
      </CardContent>
    </Card>

    <EmptyState
      v-if="!loading && !error && currentKeyword && results.length === 0"
      title="没有匹配的公开文章"
      description="可以尝试缩短关键词、调整排序，或去归档页按时间浏览文章。"
      action-label="查看归档"
      action-to="/archives"
      secondary-label="返回首页"
      secondary-to="/"
    />

    <EmptyState
      v-else-if="!loading && !error && !currentKeyword"
      title="输入关键词开始搜索"
      description="可以搜索公开文章的标题、摘要、正文内容和访问标识。"
      action-label="浏览首页"
      action-to="/"
    />

    <PublicPagination
      v-if="currentKeyword && !error && total > 0"
      :page-no="pageNo"
      :total-pages="totalPages"
      :total="total"
      :loading="loading"
      @previous="goPage(pageNo - 1)"
      @next="goPage(pageNo + 1)"
    />
  </div>
</template>
