<script setup lang="ts">
import { Download, Plus } from 'lucide-vue-next'
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  batchDeleteArticlesApi,
  batchUpdateArticleCategoryApi,
  batchUpdateArticleVisibilityApi,
  deleteArticleApi,
  exportArticleMarkdownApi,
  exportArticlesMarkdownApi,
  pageArticlesApi,
  updateArticleTopApi,
  updateArticleVisibilityApi,
  type ArticleListItem,
} from '../../api/article'
import { pageColumnsApi, type ColumnItem } from '../../api/column'
import { pageCategoriesApi, pageTagsApi, type CategoryItem, type TagItem } from '../../api/taxonomy'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Checkbox } from '../../components/ui/checkbox'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'

const router = useRouter()
const articles = ref<ArticleListItem[]>([])
const categories = ref<CategoryItem[]>([])
const columns = ref<ColumnItem[]>([])
const tags = ref<TagItem[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = 10
const keyword = ref('')
const categoryId = ref('')
const columnId = ref('')
const tagId = ref('')
const status = ref('')
const visibility = ref('')
const loading = ref(false)
const exporting = ref(false)
const message = ref('')
const error = ref('')
const selectedArticleIds = ref<number[]>([])
const batchCategoryId = ref('')

const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))
const selectedCount = computed(() => selectedArticleIds.value.length)
const allPageSelected = computed(() => articles.value.length > 0 && articles.value.every((item) => selectedArticleIds.value.includes(item.id)))

const loadOptions = async () => {
  const [categoryPage, columnPage, tagPage] = await Promise.all([
    pageCategoriesApi({ pageNo: 1, pageSize: 100 }),
    pageColumnsApi({ pageNo: 1, pageSize: 100 }),
    pageTagsApi({ pageNo: 1, pageSize: 100 }),
  ])
  categories.value = categoryPage.records
  columns.value = columnPage.records
  tags.value = tagPage.records
}

const loadArticles = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await pageArticlesApi({
      pageNo: pageNo.value,
      pageSize,
      keyword: keyword.value,
      categoryId: categoryId.value ? Number(categoryId.value) : undefined,
      columnId: columnId.value ? Number(columnId.value) : undefined,
      tagId: tagId.value ? Number(tagId.value) : undefined,
      status: status.value !== '' ? Number(status.value) : undefined,
      visibility: visibility.value !== '' ? Number(visibility.value) : undefined,
    })
    articles.value = page.records
    total.value = page.total
    selectedArticleIds.value = selectedArticleIds.value.filter((id) => articles.value.some((item) => item.id === id))
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  await loadArticles()
})

watch(pageNo, loadArticles)

const search = async () => {
  pageNo.value = 1
  await loadArticles()
}

const resetFilters = async () => {
  keyword.value = ''
  categoryId.value = ''
  columnId.value = ''
  tagId.value = ''
  status.value = ''
  visibility.value = ''
  await search()
}

const toggleArticleSelection = (id: number, checked: boolean) => {
  if (checked && !selectedArticleIds.value.includes(id)) {
    selectedArticleIds.value.push(id)
  }
  if (!checked) {
    selectedArticleIds.value = selectedArticleIds.value.filter((item) => item !== id)
  }
}

const togglePageSelection = (checked: boolean) => {
  selectedArticleIds.value = checked ? articles.value.map((item) => item.id) : []
}

const clearSelection = () => {
  selectedArticleIds.value = []
  batchCategoryId.value = ''
}

const ensureSelection = () => {
  if (selectedArticleIds.value.length === 0) {
    error.value = '请先选择要操作的文章'
    return false
  }
  return true
}

const extractDownloadFileName = (contentDisposition?: unknown, fallback = 'technote-article.md') => {
  if (typeof contentDisposition !== 'string' || !contentDisposition) {
    return fallback
  }
  const encodedName = contentDisposition.match(/filename\*=UTF-8''([^;]+)/)
  if (encodedName?.[1]) {
    return decodeURIComponent(encodedName[1])
  }
  const plainName = contentDisposition.match(/filename="?([^"]+)"?/)
  return plainName?.[1] || fallback
}

const downloadBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const exportArticle = async (item: ArticleListItem) => {
  exporting.value = true
  message.value = ''
  error.value = ''
  try {
    const response = await exportArticleMarkdownApi(item.id)
    downloadBlob(response.data, extractDownloadFileName(response.headers['content-disposition'], `${item.slug}.md`))
    message.value = '文章 Markdown 已导出'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '导出失败'
  } finally {
    exporting.value = false
  }
}

const exportSelectedArticles = async () => {
  if (!ensureSelection()) {
    return
  }
  exporting.value = true
  message.value = ''
  error.value = ''
  try {
    const response = await exportArticlesMarkdownApi(selectedArticleIds.value)
    downloadBlob(response.data, extractDownloadFileName(response.headers['content-disposition'], 'technote-articles.md'))
    message.value = `已导出 ${selectedArticleIds.value.length} 篇文章`
  } catch (err) {
    error.value = err instanceof Error ? err.message : '批量导出失败'
  } finally {
    exporting.value = false
  }
}

const updateSelectedVisibility = async (value: number) => {
  if (!ensureSelection()) {
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await batchUpdateArticleVisibilityApi(selectedArticleIds.value, value)
    message.value = value === 1 ? '已批量设为公开' : '已批量设为私密'
    clearSelection()
    await loadArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '批量更新公开状态失败'
  } finally {
    loading.value = false
  }
}

const updateSelectedCategory = async () => {
  if (!ensureSelection()) {
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await batchUpdateArticleCategoryApi(
      selectedArticleIds.value,
      batchCategoryId.value ? Number(batchCategoryId.value) : undefined,
    )
    message.value = batchCategoryId.value ? '已批量修改分类' : '已批量移出分类'
    clearSelection()
    await loadArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '批量修改分类失败'
  } finally {
    loading.value = false
  }
}

const deleteSelectedArticles = async () => {
  if (!ensureSelection()) {
    return
  }
  if (!window.confirm(`确认将 ${selectedArticleIds.value.length} 篇文章移入回收站吗？`)) {
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await batchDeleteArticlesApi(selectedArticleIds.value)
    message.value = `已将 ${selectedArticleIds.value.length} 篇文章移入回收站`
    clearSelection()
    await loadArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '批量删除失败'
  } finally {
    loading.value = false
  }
}

const removeArticle = async (id: number) => {
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await deleteArticleApi(id)
    message.value = '文章已移入回收站'
    await loadArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '删除失败'
  } finally {
    loading.value = false
  }
}

const toggleTop = async (item: ArticleListItem) => {
  await updateArticleTopApi(item.id, item.topFlag === 1 ? 0 : 1)
  await loadArticles()
}

const toggleVisibility = async (item: ArticleListItem) => {
  await updateArticleVisibilityApi(item.id, item.visibility === 1 ? 0 : 1)
  await loadArticles()
}

const statusText = (value: number) => {
  if (value === 1) return '发布'
  if (value === 2) return '自动草稿'
  return '草稿'
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-xl font-semibold">文章管理</h1>
        <p class="mt-1 text-sm text-muted-foreground">管理草稿、公开文章、私密文章和专栏归属。</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" :disabled="exporting || selectedCount === 0" @click="exportSelectedArticles">
          <Download data-icon="inline-start" />
          导出已选 {{ selectedCount }}
        </Button>
        <Button @click="router.push('/admin/articles/new')">
          <Plus data-icon="inline-start" />
          新建文章
        </Button>
      </div>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <Card>
      <div v-if="selectedCount > 0" class="flex flex-wrap items-center gap-2 border-b p-3">
        <span class="text-sm text-muted-foreground">已选择 {{ selectedCount }} 篇</span>
        <Button size="sm" variant="outline" :disabled="loading" @click="updateSelectedVisibility(1)">设为公开</Button>
        <Button size="sm" variant="outline" :disabled="loading" @click="updateSelectedVisibility(0)">设为私密</Button>
        <Select v-model="batchCategoryId" class="w-40">
          <option value="">移出分类</option>
          <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
        </Select>
        <Button size="sm" variant="outline" :disabled="loading" @click="updateSelectedCategory">修改分类</Button>
        <Button size="sm" variant="outline" :disabled="loading" @click="deleteSelectedArticles">移入回收站</Button>
        <Button size="sm" variant="ghost" :disabled="loading" @click="clearSelection">清空选择</Button>
      </div>
      <div class="grid gap-3 border-b p-3 xl:grid-cols-[1fr_150px_150px_150px_130px_130px_auto]">
        <Input v-model="keyword" placeholder="搜索标题或访问标识" @keyup.enter="search" />
        <Select v-model="categoryId">
          <option value="">全部分类</option>
          <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
        </Select>
        <Select v-model="columnId">
          <option value="">全部专栏</option>
          <option v-for="item in columns" :key="item.id" :value="item.id">{{ item.name }}</option>
        </Select>
        <Select v-model="tagId">
          <option value="">全部标签</option>
          <option v-for="item in tags" :key="item.id" :value="item.id">{{ item.name }}</option>
        </Select>
        <Select v-model="status">
          <option value="">全部状态</option>
          <option value="1">发布</option>
          <option value="0">草稿</option>
          <option value="2">自动草稿</option>
        </Select>
        <Select v-model="visibility">
          <option value="">全部可见性</option>
          <option value="1">公开</option>
          <option value="0">私密</option>
        </Select>
        <div class="flex gap-2">
          <Button variant="outline" :disabled="loading" @click="search">搜索</Button>
          <Button variant="ghost" :disabled="loading" @click="resetFilters">重置</Button>
        </div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="w-10">
              <Checkbox :checked="allPageSelected" :disabled="articles.length === 0" @update:checked="togglePageSelection" />
            </TableHead>
            <TableHead>标题</TableHead>
            <TableHead>分类/专栏</TableHead>
            <TableHead>标签</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>阅读</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in articles" :key="item.id">
            <TableCell>
              <Checkbox
                :checked="selectedArticleIds.includes(item.id)"
                :disabled="loading || exporting"
                @update:checked="toggleArticleSelection(item.id, $event)"
              />
            </TableCell>
            <TableCell>
              <div class="min-w-0">
                <div class="truncate font-medium">{{ item.title }}</div>
                <div class="truncate text-xs text-muted-foreground">{{ item.slug }}</div>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-col gap-1 text-sm">
                <span>{{ item.categoryName || '未分类' }}</span>
                <span class="text-xs text-muted-foreground">{{ item.columnName || '未加入专栏' }}</span>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-wrap gap-1">
                <Badge v-for="tag in item.tagNames" :key="tag" variant="secondary">{{ tag }}</Badge>
                <span v-if="item.tagNames.length === 0" class="text-muted-foreground">-</span>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-wrap gap-1">
                <Badge :variant="item.status === 1 ? 'secondary' : 'outline'">{{ statusText(item.status) }}</Badge>
                <Badge :variant="item.visibility === 1 ? 'secondary' : 'outline'">
                  {{ item.visibility === 1 ? '公开' : '私密' }}
                </Badge>
                <Badge v-if="item.topFlag === 1">置顶</Badge>
              </div>
            </TableCell>
            <TableCell>{{ item.viewCount }}</TableCell>
            <TableCell>
              <div class="flex flex-wrap gap-2">
                <Button variant="ghost" @click="router.push(`/admin/articles/${item.id}/edit`)">编辑</Button>
                <Button variant="ghost" @click="toggleTop(item)">
                  {{ item.topFlag === 1 ? '取消置顶' : '置顶' }}
                </Button>
                <Button variant="ghost" @click="toggleVisibility(item)">
                  {{ item.visibility === 1 ? '设为私密' : '设为公开' }}
                </Button>
                <Button variant="ghost" :disabled="exporting" @click="exportArticle(item)">导出</Button>
                <Button variant="ghost" @click="removeArticle(item.id)">删除</Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="articles.length === 0">
            <TableCell colspan="7" class="py-10 text-center text-muted-foreground">暂无文章</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <div class="flex items-center justify-between p-3 text-sm text-muted-foreground">
        <span>第 {{ pageNo }} / {{ totalPages() }} 页，共 {{ total }} 条</span>
        <div class="flex gap-2">
          <Button variant="outline" :disabled="pageNo <= 1 || loading" @click="pageNo--">上一页</Button>
          <Button variant="outline" :disabled="pageNo >= totalPages() || loading" @click="pageNo++">下一页</Button>
        </div>
      </div>
    </Card>
  </div>
</template>
