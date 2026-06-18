<script setup lang="ts">
import { LoaderCircle, RefreshCw } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import {
  listPublicGuestbooksApi,
  submitGuestbookApi,
  type GuestbookItem,
  type GuestbookPayload,
} from '../../api/comment'
import { createCaptchaApi, type CaptchaItem } from '../../api/guard'
import { Alert } from '../../components/ui/alert'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Textarea } from '../../components/ui/textarea'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'

const guestbooks = ref<GuestbookItem[]>([])
const loading = ref(true)
const submitting = ref(false)
const loadError = ref('')
const submitError = ref('')
const message = ref('')
const captcha = ref<CaptchaItem>()
const captchaAnswer = ref('')
const captchaLoading = ref(false)
const captchaError = ref('')
const form = reactive<GuestbookPayload>({
  nickname: '',
  email: '',
  content: '',
})

const loadGuestbooks = async () => {
  loading.value = true
  loadError.value = ''
  try {
    guestbooks.value = await listPublicGuestbooksApi()
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : '留言加载失败'
    guestbooks.value = []
  } finally {
    loading.value = false
  }
}

const loadCaptcha = async () => {
  captchaLoading.value = true
  captchaError.value = ''
  try {
    captcha.value = await createCaptchaApi()
  } catch (err) {
    captcha.value = undefined
    captchaError.value = err instanceof Error ? err.message : '验证码加载失败'
  } finally {
    captchaLoading.value = false
  }
}

const submitGuestbook = async () => {
  if (submitting.value) return
  submitError.value = ''
  message.value = ''
  const nickname = form.nickname.trim()
  const content = form.content.trim()
  if (!nickname || !content) {
    submitError.value = '请填写昵称和留言内容'
    return
  }
  const captchaId = captcha.value?.captchaId
  const captchaAnswerValue = captchaAnswer.value.trim()
  const captchaRequired = captcha.value?.expireSeconds !== 0
  if (captchaRequired && (!captchaId || !captchaAnswerValue)) {
    submitError.value = '请先完成验证码'
    return
  }
  submitting.value = true
  try {
    await submitGuestbookApi({
      nickname,
      email: form.email?.trim() || undefined,
      content,
      captchaId: captchaId || undefined,
      captchaAnswer: captchaAnswerValue || undefined,
    })
    form.nickname = ''
    form.email = ''
    form.content = ''
    captchaAnswer.value = ''
    message.value = '留言已提交，审核通过后会展示'
  } catch (err) {
    submitError.value = err instanceof Error ? err.message : '留言提交失败'
  } finally {
    submitting.value = false
    await loadCaptcha()
  }
}

onMounted(() => {
  loadGuestbooks()
  loadCaptcha()
})
</script>

<template>
  <div class="flex flex-col gap-4">
    <Card>
      <CardHeader>
        <CardTitle>留言板</CardTitle>
        <p class="text-sm text-muted-foreground">留言默认进入审核队列，通过后公开展示。</p>
      </CardHeader>
      <CardContent class="flex flex-col gap-3">
        <Alert v-if="message || submitError" :variant="submitError ? 'destructive' : 'success'">
          {{ submitError || message }}
        </Alert>
        <form class="grid gap-3 sm:grid-cols-2" :aria-busy="submitting" @submit.prevent="submitGuestbook">
          <Input v-model="form.nickname" :disabled="submitting" placeholder="昵称" />
          <Input v-model="form.email" :disabled="submitting" placeholder="邮箱，可选" />
          <Textarea v-model="form.content" class="sm:col-span-2" :disabled="submitting" placeholder="写下留言" />
          <div class="flex flex-col gap-2 sm:col-span-2">
            <div class="flex flex-wrap items-center gap-2 text-sm">
              <span class="text-muted-foreground">验证码</span>
              <span class="rounded-md border bg-surface px-3 py-1 font-medium">
                {{ captchaLoading ? '加载中' : captcha?.question || '加载失败' }}
              </span>
              <Button type="button" variant="ghost" size="sm" :disabled="submitting || captchaLoading" @click="loadCaptcha">
                <RefreshCw data-icon="inline-start" />
                换一题
              </Button>
            </div>
            <Input
              v-model="captchaAnswer"
              inputmode="numeric"
              autocomplete="off"
              :disabled="submitting || captchaLoading"
              placeholder="输入计算结果"
            />
            <span v-if="captchaError" class="text-sm text-destructive">{{ captchaError }}</span>
          </div>
          <Button class="min-w-24 w-fit" type="submit" :disabled="submitting">
            <LoaderCircle v-if="submitting" class="animate-spin" data-icon="inline-start" />
            {{ submitting ? '提交中' : '提交留言' }}
          </Button>
        </form>
      </CardContent>
    </Card>

    <Alert v-if="loadError" variant="destructive">{{ loadError }}</Alert>

    <LoadingState
      v-if="loading"
      title="正在加载留言"
      description="正在读取已审核通过的公开留言。"
    />

    <Card v-for="item in guestbooks" :key="item.id">
      <CardContent class="p-4">
        <div class="flex flex-wrap items-center gap-2 text-sm">
          <span class="font-medium">{{ item.nickname }}</span>
          <span class="text-xs text-muted-foreground">{{ item.createTime }}</span>
        </div>
        <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-6">{{ item.content }}</p>
        <div v-if="item.replyContent" class="mt-3 break-words rounded-md bg-surface p-3 text-sm leading-6 text-muted-foreground">
          回复：{{ item.replyContent }}
        </div>
      </CardContent>
    </Card>

    <EmptyState
      v-if="!loading && !loadError && guestbooks.length === 0"
      title="暂无已通过留言"
      description="还没有公开展示的留言，可以使用上方表单提交第一条留言。"
      action-label="浏览文章"
      action-to="/"
    />
  </div>
</template>
