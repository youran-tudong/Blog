<script setup lang="ts">
import { ListOrdered, Pencil, Plus, Search, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { pageArticlesApi, type ArticleListItem } from '../../api/article'
import {
  addArticleToColumnApi,
  createColumnApi,
  deleteColumnApi,
  listColumnArticlesApi,
  pageColumnsApi,
  removeArticleFromColumnApi,
  updateColumnApi,
  updateColumnArticleSortApi,
  type ColumnArticleItem,
  type ColumnItem,
  type ColumnPayload,
} from '../../api/column'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'
import { Textarea } from '../../components/ui/textarea'

const toSlug = (value: string) =>
  value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')

const columns = ref<ColumnItem[]>([])
const columnArticles = ref<ColumnArticleItem[]>([])
const articleOptions = ref<ArticleListItem[]>([])
const selectedColumnId = ref<number>()
const editingColumnId = ref<number>()
const pageNo = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')
const status = ref('')
const articleKeyword = ref('')
const selectedArticleId = ref('')
const relationSortOrder = ref(0)
const loading = ref(false)
const relationLoading = ref(false)
const message = ref('')
const error = ref('')

const form = reactive<ColumnPayload>({
  name: '',
  slug: '',
  description: '',
  coverUrl: '',
  sortOrder: 0,
  status: 1,
})

const selectedColumn = computed(() => columns.value.find((item) => item.id === selectedColumnId.value))
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
const formTitle = computed(() => (editingColumnId.value ? '编辑专栏' : '新增专栏'))

const loadColumns = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await pageColumnsApi({
      pageNo: pageNo.value,
      pageSize,
      keyword: keyword.value,
      status: status.value !== '' ? Number(status.value) : undefined,
    })
    columns.value = page.records
    total.value = page.total
    if (!selectedColumnId.value || !columns.value.some((item) => item.id === selectedColumnId.value)) {
      selectedColumnId.value = columns.value[0]?.id
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '专栏加载失败'
  } finally {
    loading.value = false
  }
}

const loadColumnArticles = async () => {
  if (!selectedColumnId.value) {
    columnArticles.value = []
    return
  }
  relationLoading.value = true
  error.value = ''
  try {
    columnArticles.value = await listColumnArticlesApi(selectedColumnId.value)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '专栏文章加载失败'
  } finally {
    relationLoading.value = false
  }
}

const searchArticles = async () => {
  relationLoading.value = true
  error.value = ''
  try {
    const page = await pageArticlesApi({
      pageNo: 1,
      pageSize: 20,
      keyword: articleKeyword.value,
    })
    articleOptions.value = page.records
    if (!selectedArticleId.value && page.records.length > 0) {
      selectedArticleId.value = String(page.records[0].id)
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章搜索失败'
  } finally {
    relationLoading.value = false
  }
}

const resetForm = () => {
  editingColumnId.value = undefined
  form.name = ''
  form.slug = ''
  form.description = ''
  form.coverUrl = ''
  form.sortOrder = 0
  form.status = 1
}

const updateFormName = (value: string | number) => {
  form.name = String(value)
  if (!form.slug) {
    form.slug = toSlug(form.name)
  }
}

const updateFormSlug = (value: string | number) => {
  form.slug = toSlug(String(value))
}

const editColumn = (item: ColumnItem) => {
  editingColumnId.value = item.id
  form.name = item.name
  form.slug = item.slug
  form.description = item.description || ''
  form.coverUrl = item.coverUrl || ''
  form.sortOrder = item.sortOrder
  form.status = item.status
}

const submitColumn = async () => {
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    const payload = { ...form, slug: toSlug(form.slug) }
    if (editingColumnId.value) {
      const updated = await updateColumnApi(editingColumnId.value, payload)
      message.value = '专栏已更新'
      selectedColumnId.value = updated.id
    } else {
      const created = await createColumnApi(payload)
      message.value = '专栏已创建'
      selectedColumnId.value = created.id
    }
    resetForm()
    await loadColumns()
    await loadColumnArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '专栏保存失败'
  } finally {
    loading.value = false
  }
}

const removeColumn = async (item: ColumnItem) => {
  if (item.articleCount > 0) {
    error.value = '请先移除专栏下的文章，再删除专栏'
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await deleteColumnApi(item.id)
    message.value = '专栏已删除'
    if (selectedColumnId.value === item.id) {
      selectedColumnId.value = undefined
    }
    await loadColumns()
    await loadColumnArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '专栏删除失败'
  } finally {
    loading.value = false
  }
}

const addArticle = async () => {
  if (!selectedColumnId.value || !selectedArticleId.value) {
    error.value = '请选择专栏和文章'
    return
  }
  relationLoading.value = true
  message.value = ''
  error.value = ''
  try {
    await addArticleToColumnApi(selectedColumnId.value, {
      articleId: Number(selectedArticleId.value),
      sortOrder: relationSortOrder.value > 0 ? relationSortOrder.value : undefined,
    })
    message.value = '文章已加入专栏'
    selectedArticleId.value = ''
    relationSortOrder.value = 0
    await loadColumnArticles()
    await loadColumns()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章加入专栏失败'
  } finally {
    relationLoading.value = false
  }
}

const saveArticleSort = async (item: ColumnArticleItem) => {
  if (!selectedColumnId.value) {
    return
  }
  relationLoading.value = true
  message.value = ''
  error.value = ''
  try {
    await updateColumnArticleSortApi(selectedColumnId.value, item.articleId, item.sortOrder)
    message.value = '文章排序已更新'
    await loadColumnArticles()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章排序更新失败'
  } finally {
    relationLoading.value = false
  }
}

const removeArticle = async (item: ColumnArticleItem) => {
  if (!selectedColumnId.value) {
    return
  }
  relationLoading.value = true
  message.value = ''
  error.value = ''
  try {
    await removeArticleFromColumnApi(selectedColumnId.value, item.articleId)
    message.value = '文章已移出专栏'
    await loadColumnArticles()
    await loadColumns()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章移出专栏失败'
  } finally {
    relationLoading.value = false
  }
}

const searchColumns = async () => {
  pageNo.value = 1
  await loadColumns()
  await loadColumnArticles()
}

const resetFilters = async () => {
  keyword.value = ''
  status.value = ''
  pageNo.value = 1
  await loadColumns()
  await loadColumnArticles()
}

const statusText = (value: number) => (value === 1 ? '显示' : '隐藏')
const articleStatusText = (value: number) => {
  if (value === 1) return '发布'
  if (value === 2) return '自动草稿'
  return '草稿'
}

watch(selectedColumnId, loadColumnArticles)
watch(pageNo, loadColumns)

onMounted(async () => {
  await loadColumns()
  await loadColumnArticles()
  await searchArticles()
})
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-xl font-semibold">专栏管理</h1>
        <p class="mt-1 text-sm text-muted-foreground">管理系列内容、前台专栏入口和专栏内文章顺序。</p>
      </div>
      <Button @click="resetForm">
        <Plus data-icon="inline-start" />
        新增专栏
      </Button>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <div class="grid gap-4 xl:grid-cols-[1fr_360px]">
      <Card class="overflow-hidden">
        <div class="grid gap-3 border-b p-3 lg:grid-cols-[1fr_140px_auto]">
          <Input v-model="keyword" placeholder="搜索专栏名称或访问标识" @keyup.enter="searchColumns" />
          <Select v-model="status">
            <option value="">全部状态</option>
            <option value="1">显示</option>
            <option value="0">隐藏</option>
          </Select>
          <div class="flex gap-2">
            <Button variant="outline" :disabled="loading" @click="searchColumns">
              <Search data-icon="inline-start" />
              搜索
            </Button>
            <Button variant="ghost" :disabled="loading" @click="resetFilters">重置</Button>
          </div>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>专栏</TableHead>
              <TableHead>访问标识</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>排序</TableHead>
              <TableHead>文章数</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="item in columns"
              :key="item.id"
              class="cursor-pointer"
              @click="selectedColumnId = item.id"
            >
              <TableCell>
                <div class="min-w-0">
                  <div class="truncate font-medium">{{ item.name }}</div>
                  <div class="line-clamp-1 text-xs text-muted-foreground">{{ item.description || '暂无简介' }}</div>
                </div>
              </TableCell>
              <TableCell class="text-muted-foreground">{{ item.slug }}</TableCell>
              <TableCell>
                <Badge :variant="item.status === 1 ? 'secondary' : 'outline'">{{ statusText(item.status) }}</Badge>
              </TableCell>
              <TableCell>{{ item.sortOrder }}</TableCell>
              <TableCell>{{ item.articleCount }}</TableCell>
              <TableCell>
                <div class="flex flex-wrap gap-2">
                  <Button variant="ghost" @click.stop="editColumn(item)">
                    <Pencil data-icon="inline-start" />
                    编辑
                  </Button>
                  <Button variant="ghost" :disabled="item.articleCount > 0 || loading" @click.stop="removeColumn(item)">
                    <Trash2 data-icon="inline-start" />
                    删除
                  </Button>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-if="columns.length === 0">
              <TableCell colspan="6" class="py-10 text-center text-muted-foreground">暂无专栏</TableCell>
            </TableRow>
          </TableBody>
        </Table>

        <div class="flex items-center justify-between p-3 text-sm text-muted-foreground">
          <span>第 {{ pageNo }} / {{ totalPages }} 页，共 {{ total }} 条</span>
          <div class="flex gap-2">
            <Button variant="outline" :disabled="pageNo <= 1 || loading" @click="pageNo--">上一页</Button>
            <Button variant="outline" :disabled="pageNo >= totalPages || loading" @click="pageNo++">下一页</Button>
          </div>
        </div>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{{ formTitle }}</CardTitle>
        </CardHeader>
        <CardContent>
          <form class="flex flex-col gap-3" @submit.prevent="submitColumn">
            <label class="block text-sm">
              专栏名称
              <Input :model-value="form.name" class="mt-1" @update:model-value="updateFormName" />
            </label>
            <label class="block text-sm">
              访问标识
              <Input :model-value="form.slug" class="mt-1" @update:model-value="updateFormSlug" />
            </label>
            <label class="block text-sm">
              简介
              <Textarea v-model="form.description" class="mt-1" />
            </label>
            <label class="block text-sm">
              封面地址
              <Input v-model="form.coverUrl" class="mt-1" />
            </label>
            <div class="grid grid-cols-2 gap-3">
              <label class="block text-sm">
                排序
                <Input v-model="form.sortOrder" type="number" class="mt-1" />
              </label>
              <label class="block text-sm">
                状态
                <Select
                  :model-value="form.status"
                  class="mt-1"
                  @update:model-value="form.status = Number($event)"
                >
                  <option :value="1">显示</option>
                  <option :value="0">隐藏</option>
                </Select>
              </label>
            </div>
            <div class="flex gap-2">
              <Button type="submit" :disabled="loading">保存专栏</Button>
              <Button type="button" variant="outline" @click="resetForm">重置</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <CardTitle class="flex items-center gap-2">
            <ListOrdered data-icon="inline-start" />
            {{ selectedColumn ? `${selectedColumn.name} · 文章排序` : '选择专栏后管理文章' }}
          </CardTitle>
          <Badge variant="outline">{{ columnArticles.length }} 篇文章</Badge>
        </div>
      </CardHeader>
      <CardContent class="flex flex-col gap-4">
        <div class="grid gap-3 lg:grid-cols-[1fr_220px_120px_auto]">
          <Input v-model="articleKeyword" placeholder="搜索可加入的文章" @keyup.enter="searchArticles" />
          <Select v-model="selectedArticleId">
            <option value="">选择文章</option>
            <option v-for="item in articleOptions" :key="item.id" :value="item.id">{{ item.title }}</option>
          </Select>
          <Input v-model="relationSortOrder" type="number" placeholder="0 自动" />
          <div class="flex gap-2">
            <Button variant="outline" :disabled="relationLoading" @click="searchArticles">搜文章</Button>
            <Button :disabled="!selectedColumnId || !selectedArticleId || relationLoading" @click="addArticle">
              加入
            </Button>
          </div>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>文章</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>排序</TableHead>
              <TableHead>更新时间</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in columnArticles" :key="item.articleId">
              <TableCell>
                <div class="min-w-0">
                  <div class="truncate font-medium">{{ item.title }}</div>
                  <div class="truncate text-xs text-muted-foreground">{{ item.slug }}</div>
                </div>
              </TableCell>
              <TableCell>
                <div class="flex flex-wrap gap-1">
                  <Badge :variant="item.status === 1 ? 'secondary' : 'outline'">{{ articleStatusText(item.status) }}</Badge>
                  <Badge :variant="item.visibility === 1 ? 'secondary' : 'outline'">
                    {{ item.visibility === 1 ? '公开' : '私密' }}
                  </Badge>
                </div>
              </TableCell>
              <TableCell class="w-28">
                <Input v-model="item.sortOrder" type="number" />
              </TableCell>
              <TableCell class="text-muted-foreground">{{ item.updateTime || item.publishTime || '-' }}</TableCell>
              <TableCell>
                <div class="flex flex-wrap gap-2">
                  <Button variant="ghost" :disabled="relationLoading" @click="saveArticleSort(item)">保存排序</Button>
                  <Button variant="ghost" :disabled="relationLoading" @click="removeArticle(item)">移出</Button>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-if="columnArticles.length === 0">
              <TableCell colspan="5" class="py-10 text-center text-muted-foreground">
                当前专栏暂无文章，可先搜索文章后加入。
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
