<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  cleanupArticleAutoDraftsApi,
  deleteArticleAutoDraftByIdApi,
  pageArticleAutoDraftsApi,
  type ArticleAutoDraftListItem,
} from '../../api/article'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'

const records = ref<ArticleAutoDraftListItem[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = 10
const keyword = ref('')
const onlyOrphan = ref('')
const cleanupDays = ref(30)
const cleanupOnlyOrphan = ref('true')
const loading = ref(false)
const message = ref('')
const error = ref('')

const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))

const visibilityText = (value: number) => (value === 0 ? '私密' : '公开')

const formatTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ').slice(0, 19)
}

const formatContentLength = (value: number) => `${value || 0} 字`

const loadDrafts = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await pageArticleAutoDraftsApi({
      pageNo: pageNo.value,
      pageSize,
      keyword: keyword.value,
      onlyOrphan: onlyOrphan.value === '' ? undefined : onlyOrphan.value === 'true',
    })
    records.value = page.records
    total.value = page.total
  } catch (err) {
    error.value = err instanceof Error ? err.message : '自动草稿加载失败'
  } finally {
    loading.value = false
  }
}

const search = async () => {
  pageNo.value = 1
  await loadDrafts()
}

const resetFilters = async () => {
  keyword.value = ''
  onlyOrphan.value = ''
  await search()
}

const deleteDraft = async (item: ArticleAutoDraftListItem) => {
  if (!window.confirm(`确认删除自动草稿「${item.title || item.draftKey}」吗？删除后不能恢复。`)) {
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await deleteArticleAutoDraftByIdApi(item.id)
    message.value = '自动草稿已删除'
    await loadDrafts()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '自动草稿删除失败'
  } finally {
    loading.value = false
  }
}

const cleanupDrafts = async () => {
  if (cleanupDays.value < 1) {
    error.value = '保留天数必须大于 0'
    return
  }
  const scopeText = cleanupOnlyOrphan.value === 'true' ? '未关联正式文章的草稿' : '全部自动草稿'
  if (!window.confirm(`确认清理 ${cleanupDays.value} 天前的${scopeText}吗？该操作不能恢复。`)) {
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    const result = await cleanupArticleAutoDraftsApi({
      retentionDays: cleanupDays.value,
      onlyOrphan: cleanupOnlyOrphan.value === 'true',
    })
    message.value = `已清理 ${result.deletedCount} 条自动草稿，截止时间 ${formatTime(result.cutoffTime)}`
    await loadDrafts()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '自动草稿清理失败'
  } finally {
    loading.value = false
  }
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await loadDrafts()
}

onMounted(loadDrafts)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <h1 class="text-xl font-semibold">自动草稿</h1>
      <p class="mt-1 text-sm text-muted-foreground">查看编辑器自动保存快照，并清理不再需要的历史草稿。</p>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <Card>
      <CardContent class="grid gap-3 p-3 lg:grid-cols-[1fr_180px_auto]">
        <Input v-model="keyword" placeholder="搜索标题、slug 或草稿键" @keyup.enter="search" />
        <Select v-model="onlyOrphan">
          <option value="">全部草稿</option>
          <option value="true">只看新文章草稿</option>
          <option value="false">包含关联文章草稿</option>
        </Select>
        <div class="flex flex-wrap gap-2">
          <Button variant="outline" :disabled="loading" @click="search">搜索</Button>
          <Button variant="ghost" :disabled="loading" @click="resetFilters">重置</Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardHeader class="gap-3">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <CardTitle class="text-base">过期清理</CardTitle>
          <Badge variant="outline">默认只清理新文章草稿</Badge>
        </div>
      </CardHeader>
      <CardContent class="grid gap-3 md:grid-cols-[160px_220px_auto]">
        <Input v-model.number="cleanupDays" type="number" min="1" max="3650" />
        <Select v-model="cleanupOnlyOrphan">
          <option value="true">只清理新文章草稿</option>
          <option value="false">清理全部过期草稿</option>
        </Select>
        <Button variant="destructive" :disabled="loading" @click="cleanupDrafts">清理过期草稿</Button>
      </CardContent>
    </Card>

    <Card>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>草稿</TableHead>
            <TableHead>关联</TableHead>
            <TableHead>正文</TableHead>
            <TableHead>保存时间</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in records" :key="item.id">
            <TableCell>
              <div class="flex max-w-[520px] flex-col gap-1">
                <div class="truncate font-medium">{{ item.title || '未命名草稿' }}</div>
                <div class="truncate text-xs text-muted-foreground">{{ item.draftKey }}</div>
                <div v-if="item.slug" class="truncate text-xs text-muted-foreground">slug: {{ item.slug }}</div>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-col gap-1">
                <Badge :variant="item.articleId ? 'secondary' : 'outline'">
                  {{ item.articleId ? `文章 ${item.articleId}` : '新文章' }}
                </Badge>
                <span class="text-xs text-muted-foreground">创建人 {{ item.createBy || '-' }}</span>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex max-w-[420px] flex-col gap-1">
                <span class="text-sm">{{ formatContentLength(item.contentLength) }}</span>
                <span class="line-clamp-2 text-xs text-muted-foreground">{{ item.contentPreview || '暂无正文' }}</span>
                <span class="text-xs text-muted-foreground">{{ visibilityText(item.visibility) }}</span>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-col gap-1 text-sm">
                <span>{{ formatTime(item.updateTime) }}</span>
                <span class="text-xs text-muted-foreground">创建 {{ formatTime(item.createTime) }}</span>
              </div>
            </TableCell>
            <TableCell>
              <Button variant="destructive" :disabled="loading" @click="deleteDraft(item)">删除</Button>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && records.length === 0">
            <TableCell colspan="5" class="py-10 text-center text-muted-foreground">暂无自动草稿</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <div class="flex flex-wrap items-center justify-between gap-2 p-3 text-sm text-muted-foreground">
        <span>第 {{ pageNo }} / {{ totalPages() }} 页，共 {{ total }} 条</span>
        <div class="flex gap-2">
          <Button variant="outline" :disabled="pageNo <= 1 || loading" @click="goPage(pageNo - 1)">上一页</Button>
          <Button variant="outline" :disabled="pageNo >= totalPages() || loading" @click="goPage(pageNo + 1)">下一页</Button>
        </div>
      </div>
    </Card>
  </div>
</template>
