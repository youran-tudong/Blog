<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  auditCommentApi,
  auditGuestbookApi,
  deleteCommentApi,
  deleteGuestbookApi,
  pageAdminCommentsApi,
  pageAdminGuestbooksApi,
  type CommentItem,
  type GuestbookItem,
} from '../../api/comment'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Textarea } from '../../components/ui/textarea'

const comments = ref<CommentItem[]>([])
const guestbooks = ref<GuestbookItem[]>([])
const commentReply = ref<Record<number, string>>({})
const guestbookReply = ref<Record<number, string>>({})
const loading = ref(false)
const message = ref('')
const error = ref('')

const statusText = (status: number) => {
  if (status === 1) return '通过'
  if (status === 2) return '驳回'
  return '待审核'
}

const statusVariant = (status: number) => (status === 0 ? 'outline' : 'secondary')

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [commentPage, guestbookPage] = await Promise.all([
      pageAdminCommentsApi({ pageNo: 1, pageSize: 50 }),
      pageAdminGuestbooksApi({ pageNo: 1, pageSize: 50 }),
    ])
    comments.value = commentPage.records
    guestbooks.value = guestbookPage.records
  } catch (err) {
    error.value = err instanceof Error ? err.message : '审核列表加载失败'
  } finally {
    loading.value = false
  }
}

const auditComment = async (id: number, status: number) => {
  loading.value = true
  error.value = ''
  try {
    await auditCommentApi(id, { status, replyContent: commentReply.value[id] })
    message.value = '评论审核已更新'
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '评论审核失败'
  } finally {
    loading.value = false
  }
}

const auditGuestbook = async (id: number, status: number) => {
  loading.value = true
  error.value = ''
  try {
    await auditGuestbookApi(id, { status, replyContent: guestbookReply.value[id] })
    message.value = '留言审核已更新'
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '留言审核失败'
  } finally {
    loading.value = false
  }
}

const removeComment = async (id: number) => {
  loading.value = true
  error.value = ''
  try {
    await deleteCommentApi(id)
    message.value = '评论已删除'
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '评论删除失败'
  } finally {
    loading.value = false
  }
}

const removeGuestbook = async (id: number) => {
  loading.value = true
  error.value = ''
  try {
    await deleteGuestbookApi(id)
    message.value = '留言已删除'
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '留言删除失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <h1 class="text-xl font-semibold">互动审核</h1>
      <p class="mt-1 text-sm text-muted-foreground">审核文章评论和留言板内容，通过后才会公开展示。</p>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <div class="grid gap-4 xl:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>文章评论</CardTitle>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <div v-for="item in comments" :key="item.id" class="rounded-md border p-3">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="flex flex-wrap items-center gap-2 text-sm font-medium">
                <span>{{ item.nickname }} · {{ item.articleTitle || item.articleId }}</span>
                <Badge v-if="item.parentId" variant="outline">回复 #{{ item.parentId }}</Badge>
              </div>
              <Badge :variant="statusVariant(item.status)">{{ statusText(item.status) }}</Badge>
            </div>
            <p class="mt-2 whitespace-pre-wrap text-sm leading-6">{{ item.content }}</p>
            <Textarea v-model="commentReply[item.id]" class="mt-3" placeholder="回复内容，可选" />
            <div class="mt-3 flex flex-wrap gap-2">
              <Button :disabled="loading || item.status !== 0" @click="auditComment(item.id, 1)">通过</Button>
              <Button variant="outline" :disabled="loading || item.status !== 0" @click="auditComment(item.id, 2)">驳回</Button>
              <Button variant="ghost" :disabled="loading" @click="removeComment(item.id)">删除</Button>
            </div>
          </div>
          <div v-if="comments.length === 0" class="rounded-md border p-8 text-center text-sm text-muted-foreground">
            暂无评论
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>留言板</CardTitle>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <div v-for="item in guestbooks" :key="item.id" class="rounded-md border p-3">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="text-sm font-medium">{{ item.nickname }}</div>
              <Badge :variant="statusVariant(item.status)">{{ statusText(item.status) }}</Badge>
            </div>
            <p class="mt-2 whitespace-pre-wrap text-sm leading-6">{{ item.content }}</p>
            <Textarea v-model="guestbookReply[item.id]" class="mt-3" placeholder="回复内容，可选" />
            <div class="mt-3 flex flex-wrap gap-2">
              <Button :disabled="loading || item.status !== 0" @click="auditGuestbook(item.id, 1)">通过</Button>
              <Button variant="outline" :disabled="loading || item.status !== 0" @click="auditGuestbook(item.id, 2)">驳回</Button>
              <Button variant="ghost" :disabled="loading" @click="removeGuestbook(item.id)">删除</Button>
            </div>
          </div>
          <div v-if="guestbooks.length === 0" class="rounded-md border p-8 text-center text-sm text-muted-foreground">
            暂无留言
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
