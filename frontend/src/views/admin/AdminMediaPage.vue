<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { deleteMediaApi, pageMediaApi, type MediaItem } from '../../api/media'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'

const mediaList = ref<MediaItem[]>([])
const keyword = ref('')
const total = ref(0)
const pageNo = ref(1)
const pageSize = 12
const loading = ref(false)
const message = ref('')
const error = ref('')

const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))
const formatSize = (size: number) => {
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  if (size >= 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${size} B`
}

const loadMedia = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await pageMediaApi({ pageNo: pageNo.value, pageSize, keyword: keyword.value })
    mediaList.value = page.records
    total.value = page.total
  } catch (err) {
    error.value = err instanceof Error ? err.message : '媒体库加载失败'
  } finally {
    loading.value = false
  }
}

const search = async () => {
  pageNo.value = 1
  await loadMedia()
}

const removeMedia = async (id: number) => {
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    await deleteMediaApi(id)
    message.value = '媒体文件已删除'
    await loadMedia()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '媒体删除失败'
  } finally {
    loading.value = false
  }
}

const copyMediaPath = async (filePath: string) => {
  try {
    await navigator.clipboard?.writeText(filePath)
    message.value = '媒体地址已复制'
  } catch {
    error.value = '复制失败，请手动复制地址'
  }
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await loadMedia()
}

onMounted(loadMedia)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <h1 class="text-xl font-semibold">媒体库</h1>
      <p class="mt-1 text-sm text-muted-foreground">查看文章上传的图片，已被引用的文件不能删除。</p>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <Card>
      <CardContent class="grid gap-3 p-3 sm:grid-cols-[1fr_auto]">
        <Input v-model="keyword" placeholder="搜索原始文件名" @keyup.enter="search" />
        <Button variant="outline" :disabled="loading" @click="search">搜索</Button>
      </CardContent>
    </Card>

    <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
      <Card v-for="item in mediaList" :key="item.id" class="overflow-hidden">
        <img
          :src="item.filePath"
          :alt="item.originalName"
          class="aspect-video w-full border-b object-cover"
        />
        <CardHeader class="gap-2">
          <CardTitle class="truncate text-sm">{{ item.originalName }}</CardTitle>
          <div class="flex flex-wrap gap-2 text-xs text-muted-foreground">
            <Badge variant="outline">{{ item.fileExt || 'file' }}</Badge>
            <span>{{ formatSize(item.fileSize) }}</span>
            <span>引用 {{ item.quoteCount }}</span>
          </div>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <div class="truncate text-xs text-muted-foreground">{{ item.filePath }}</div>
          <div class="flex gap-2">
            <Button variant="outline" @click="copyMediaPath(item.filePath)">复制地址</Button>
            <Button variant="ghost" :disabled="loading || item.quoteCount > 0" @click="removeMedia(item.id)">删除</Button>
          </div>
        </CardContent>
      </Card>
    </div>

    <Card v-if="!loading && mediaList.length === 0">
      <CardContent class="p-8 text-center text-sm text-muted-foreground">暂无媒体文件</CardContent>
    </Card>

    <div class="flex flex-wrap items-center justify-between gap-2 text-sm text-muted-foreground">
      <span>第 {{ pageNo }} / {{ totalPages() }} 页，共 {{ total }} 条</span>
      <div class="flex gap-2">
        <Button variant="outline" :disabled="pageNo <= 1 || loading" @click="goPage(pageNo - 1)">上一页</Button>
        <Button variant="outline" :disabled="pageNo >= totalPages() || loading" @click="goPage(pageNo + 1)">下一页</Button>
      </div>
    </div>
  </div>
</template>
