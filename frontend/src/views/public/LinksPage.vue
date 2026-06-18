<script setup lang="ts">
import { ChevronDown, ChevronUp, ExternalLink, LoaderCircle, RefreshCw, Send } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import {
  listPublicLinksApi,
  submitLinkApplyApi,
  type LinkApplyPayload,
  type LinkItem,
} from '../../api/link'
import { createCaptchaApi, type CaptchaItem } from '../../api/guard'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import { Alert } from '../../components/ui/alert'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Textarea } from '../../components/ui/textarea'

const links = ref<LinkItem[]>([])
const loading = ref(true)
const submitting = ref(false)
const applicationOpen = ref(false)
const loadError = ref('')
const submitError = ref('')
const message = ref('')
const captcha = ref<CaptchaItem>()
const captchaAnswer = ref('')
const captchaLoading = ref(false)
const captchaError = ref('')

const form = reactive({
  siteName: '',
  siteUrl: '',
  iconUrl: '',
  description: '',
  applicantEmail: '',
})

const isHttpUrl = (value: string) => {
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

const loadLinks = async () => {
  loading.value = true
  loadError.value = ''
  try {
    links.value = await listPublicLinksApi()
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : '友链加载失败'
    links.value = []
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

const submitApplication = async () => {
  if (submitting.value) return
  submitError.value = ''
  message.value = ''

  const payload: LinkApplyPayload = {
    siteName: form.siteName.trim(),
    siteUrl: form.siteUrl.trim(),
    iconUrl: form.iconUrl.trim() || undefined,
    description: form.description.trim() || undefined,
    applicantEmail: form.applicantEmail.trim(),
    captchaId: captcha.value?.captchaId || undefined,
    captchaAnswer: captchaAnswer.value.trim() || undefined,
  }
  if (!payload.siteName || !payload.siteUrl || !payload.applicantEmail) {
    submitError.value = '请填写网站名称、网站地址和联系邮箱'
    return
  }
  if (!isHttpUrl(payload.siteUrl)) {
    submitError.value = '网站地址必须以 http:// 或 https:// 开头'
    return
  }
  if (payload.iconUrl && !isHttpUrl(payload.iconUrl)) {
    submitError.value = '图标地址必须以 http:// 或 https:// 开头'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.applicantEmail)) {
    submitError.value = '请填写正确的联系邮箱'
    return
  }

  const captchaRequired = captcha.value?.expireSeconds !== 0
  if (captchaRequired && (!payload.captchaId || !payload.captchaAnswer)) {
    submitError.value = '请先完成验证码'
    return
  }

  submitting.value = true
  try {
    await submitLinkApplyApi(payload)
    form.siteName = ''
    form.siteUrl = ''
    form.iconUrl = ''
    form.description = ''
    form.applicantEmail = ''
    captchaAnswer.value = ''
    message.value = '友链申请已提交，审核通过后会由管理员确认是否公开展示'
  } catch (err) {
    submitError.value = err instanceof Error ? err.message : '友链申请提交失败'
  } finally {
    submitting.value = false
    await loadCaptcha()
  }
}

onMounted(() => {
  loadLinks()
  loadCaptcha()
})
</script>

<template>
  <div class="flex flex-col gap-4">
    <Card>
      <CardHeader>
        <CardTitle>友情链接</CardTitle>
        <CardDescription>发现值得持续阅读的技术站点，也欢迎提交你的站点。</CardDescription>
      </CardHeader>
      <CardContent>
        <Button variant="outline" :aria-expanded="applicationOpen" @click="applicationOpen = !applicationOpen">
          <Send />
          申请友链
          <ChevronUp v-if="applicationOpen" />
          <ChevronDown v-else />
        </Button>
      </CardContent>
    </Card>

    <Card v-if="applicationOpen">
      <CardHeader>
        <CardTitle class="text-base">提交友链申请</CardTitle>
        <CardDescription>申请会进入待审核队列，审核通过后先以隐藏状态加入友链管理。</CardDescription>
      </CardHeader>
      <CardContent class="flex flex-col gap-3">
        <Alert v-if="message || submitError" :variant="submitError ? 'destructive' : 'success'">
          {{ submitError || message }}
        </Alert>
        <form class="grid gap-3 sm:grid-cols-2" :aria-busy="submitting" @submit.prevent="submitApplication">
          <label class="flex flex-col gap-1 text-sm">
            网站名称
            <Input v-model="form.siteName" :disabled="submitting" maxlength="100" placeholder="TechNote" />
          </label>
          <label class="flex flex-col gap-1 text-sm">
            联系邮箱
            <Input
              v-model="form.applicantEmail"
              type="email"
              :disabled="submitting"
              maxlength="128"
              placeholder="name@example.com"
            />
          </label>
          <label class="flex flex-col gap-1 text-sm sm:col-span-2">
            网站地址
            <Input
              v-model="form.siteUrl"
              type="url"
              :disabled="submitting"
              maxlength="255"
              placeholder="https://example.com"
            />
          </label>
          <label class="flex flex-col gap-1 text-sm sm:col-span-2">
            图标地址
            <Input
              v-model="form.iconUrl"
              type="url"
              :disabled="submitting"
              maxlength="500"
              placeholder="https://example.com/favicon.ico，可选"
            />
          </label>
          <label class="flex flex-col gap-1 text-sm sm:col-span-2">
            网站简介
            <Textarea
              v-model="form.description"
              :disabled="submitting"
              maxlength="255"
              placeholder="用一句话介绍你的站点，可选"
            />
          </label>
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
          <Button class="w-fit min-w-28" type="submit" :disabled="submitting">
            <LoaderCircle v-if="submitting" class="animate-spin" data-icon="inline-start" />
            <Send v-else data-icon="inline-start" />
            {{ submitting ? '提交中' : '提交申请' }}
          </Button>
        </form>
      </CardContent>
    </Card>

    <Alert v-if="loadError" variant="destructive">{{ loadError }}</Alert>

    <LoadingState
      v-if="loading"
      title="正在加载友情链接"
      description="正在读取当前公开展示的站点。"
    />

    <div v-if="!loading && !loadError" class="grid gap-3 sm:grid-cols-2">
      <a
        v-for="item in links"
        :key="item.id"
        :href="item.siteUrl"
        target="_blank"
        rel="noreferrer"
        class="rounded-md border bg-background p-4 transition-colors hover:bg-hover"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex min-w-0 items-center gap-3">
            <img
              v-if="item.iconUrl"
              :src="item.iconUrl"
              :alt="item.siteName"
              class="size-10 shrink-0 rounded-md border object-cover"
            />
            <div class="min-w-0">
              <div class="break-words font-medium">{{ item.siteName }}</div>
              <p class="mt-1 line-clamp-2 break-words text-sm leading-6 text-muted-foreground">
                {{ item.description || item.siteUrl }}
              </p>
            </div>
          </div>
          <ExternalLink class="shrink-0 text-muted-foreground" :size="16" />
        </div>
      </a>
    </div>

    <EmptyState
      v-if="!loading && !loadError && links.length === 0"
      title="暂无公开友链"
      description="当前还没有公开展示的友情链接，你可以提交第一个友链申请。"
      action-label="返回首页"
      action-to="/"
      secondary-label="前往留言板"
      secondary-to="/guestbook"
    />
  </div>
</template>
