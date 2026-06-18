<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  deleteRecyclePermanentlyApi,
  pageRecycleApi,
  restoreRecycleApi,
  type RecycleItem,
} from '../../api/recycle'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'

const records = ref<RecycleItem[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = 10
const keyword = ref('')
const resourceType = ref('ARTICLE')
const loading = ref(false)
const message = ref('')
const error = ref('')

const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))

const resourceTypeText = (value: string) => {
  if (value === 'ARTICLE') return '文章'
  return value
}

const loadRecycle = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await pageRecycleApi({
      pageNo: pageNo.value,
      pageSize,
      keyword: keyword.value,
      resourceType: resourceType.value || undefined,
    })
    records.value = page.records
    total.value = page.total
  } catch (err) {
    error.value = err instanceof Error ? err.message : '回收站加载失败'
  } finally {
    loading.value = false
  }
}

const search = async () => {
  pageNo.value = 1
  await loadRecycle()
}

const resetFilters = async () => {
  keyword.value = ''
  resourceType.value = 'ARTICLE'
  await search()
}

const restoreRecord = async (item: RecycleItem) => {
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await restoreRecycleApi(item.id)
    message.value = '资源已恢复'
    await loadRecycle()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '恢复失败'
  } finally {
    loading.value = false
  }
}

const deletePermanently = async (item: RecycleItem) => {
  if (!window.confirm(`确认永久删除「${item.title || item.resourceId}」吗？该操作不能恢复。`)) {
    return
  }
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await deleteRecyclePermanentlyApi(item.id)
    message.value = '资源已永久删除'
    await loadRecycle()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '永久删除失败'
  } finally {
    loading.value = false
  }
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await loadRecycle()
}

onMounted(loadRecycle)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <h1 class="text-xl font-semibold">回收站</h1>
      <p class="mt-1 text-sm text-muted-foreground">恢复误删内容，或清理确认不再需要的删除记录。</p>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <Card>
      <CardContent class="grid gap-3 p-3 md:grid-cols-[180px_1fr_auto]">
        <Select v-model="resourceType">
          <option value="">全部类型</option>
          <option value="ARTICLE">文章</option>
        </Select>
        <Input v-model="keyword" placeholder="搜索删除内容标题" @keyup.enter="search" />
        <div class="flex flex-wrap gap-2">
          <Button variant="outline" :disabled="loading" @click="search">搜索</Button>
          <Button variant="ghost" :disabled="loading" @click="resetFilters">重置</Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>资源</TableHead>
            <TableHead>标题</TableHead>
            <TableHead>删除信息</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in records" :key="item.id">
            <TableCell>
              <div class="flex flex-col gap-1">
                <Badge variant="outline">{{ resourceTypeText(item.resourceType) }}</Badge>
                <span class="text-xs text-muted-foreground">ID {{ item.resourceId }}</span>
              </div>
            </TableCell>
            <TableCell>
              <div class="max-w-[520px] truncate font-medium">{{ item.title || '-' }}</div>
            </TableCell>
            <TableCell>
              <div class="flex flex-col gap-1 text-sm">
                <span>{{ item.deleteTime || '-' }}</span>
                <span class="text-xs text-muted-foreground">删除人 {{ item.deleteBy || '-' }}</span>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-wrap gap-2">
                <Button variant="outline" :disabled="loading" @click="restoreRecord(item)">恢复</Button>
                <Button variant="destructive" :disabled="loading" @click="deletePermanently(item)">永久删除</Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && records.length === 0">
            <TableCell colspan="4" class="py-10 text-center text-muted-foreground">暂无回收站记录</TableCell>
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
