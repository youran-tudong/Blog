<script setup lang="ts">
import { Check, ExternalLink, LoaderCircle, Trash2, X } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  auditLinkApplyApi,
  deleteLinkApplyApi,
  pageAdminLinkAppliesApi,
  type LinkApplyItem,
} from '../../api/link'
import LoadingState from '../../components/LoadingState.vue'
import PublicPagination from '../../components/PublicPagination.vue'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card'
import { Select } from '../../components/ui/select'
import { Textarea } from '../../components/ui/textarea'

const applications = ref<LinkApplyItem[]>([])
const pageNo = ref(1)
const pageSize = 10
const total = ref(0)
const statusFilter = ref<number | ''>(0)
const initialLoading = ref(true)
const loading = ref(false)
const processingId = ref<number>()
const message = ref('')
const error = ref('')
const remarks = reactive<Record<number, string>>({})
let requestId = 0

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const statusText = (status: number) => {
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '待审核'
}

const statusVariant = (status: number) => (status === 0 ? 'outline' : 'secondary')

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const loadApplications = async () => {
  const currentRequestId = ++requestId
  loading.value = true
  error.value = ''
  try {
    const page = await pageAdminLinkAppliesApi({
      pageNo: pageNo.value,
      pageSize,
      status: statusFilter.value === '' ? undefined : Number(statusFilter.value),
    })
    if (currentRequestId !== requestId) return
    applications.value = page.records
    total.value = page.total
    page.records.forEach((item) => {
      if (remarks[item.id] === undefined) remarks[item.id] = item.auditRemark || ''
    })
  } catch (err) {
    if (currentRequestId !== requestId) return
    error.value = err instanceof Error ? err.message : '友链申请加载失败'
    applications.value = []
    total.value = 0
  } finally {
    if (currentRequestId === requestId) {
      loading.value = false
      initialLoading.value = false
    }
  }
}

const changeStatus = (value: string) => {
  statusFilter.value = value === '' ? '' : Number(value)
  pageNo.value = 1
  loadApplications()
}

const auditApplication = async (item: LinkApplyItem, status: number) => {
  if (processingId.value !== undefined) return
  const auditRemark = remarks[item.id]?.trim() || undefined
  if (status === 2 && !auditRemark) {
    error.value = '驳回申请时请填写审核备注'
    return
  }

  processingId.value = item.id
  message.value = ''
  error.value = ''
  try {
    await auditLinkApplyApi(item.id, { status, auditRemark })
    message.value = status === 1 ? '申请已通过，并已创建为隐藏友链' : '申请已驳回'
    await loadApplications()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '友链申请审核失败'
  } finally {
    processingId.value = undefined
  }
}

const removeApplication = async (item: LinkApplyItem) => {
  if (processingId.value !== undefined || !window.confirm(`确认删除“${item.siteName}”的友链申请吗？`)) return
  processingId.value = item.id
  message.value = ''
  error.value = ''
  try {
    await deleteLinkApplyApi(item.id)
    message.value = '友链申请已删除'
    if (applications.value.length === 1 && pageNo.value > 1) pageNo.value -= 1
    await loadApplications()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '友链申请删除失败'
  } finally {
    processingId.value = undefined
  }
}

const previousPage = () => {
  if (pageNo.value <= 1 || loading.value) return
  pageNo.value -= 1
  loadApplications()
}

const nextPage = () => {
  if (pageNo.value >= totalPages.value || loading.value) return
  pageNo.value += 1
  loadApplications()
}

onMounted(loadApplications)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 class="text-xl font-semibold">友链申请</h1>
        <p class="mt-1 text-sm text-muted-foreground">
          审核通过后会创建为隐藏友链，请在友链管理中确认内容并设置为显示。
        </p>
      </div>
      <label class="flex min-w-40 flex-col gap-1 text-sm">
        审核状态
        <Select :model-value="statusFilter" :disabled="loading" @update:model-value="changeStatus">
          <option value="">全部状态</option>
          <option :value="0">待审核</option>
          <option :value="1">已通过</option>
          <option :value="2">已驳回</option>
        </Select>
      </label>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <LoadingState
      v-if="initialLoading"
      title="正在加载友链申请"
      description="正在读取待处理的申请记录。"
    />

    <div
      v-if="loading && !initialLoading"
      role="status"
      aria-live="polite"
      class="flex items-center gap-2 text-sm text-muted-foreground"
    >
      <LoaderCircle class="animate-spin" />
      正在刷新申请列表
    </div>

    <div v-if="!initialLoading" class="grid gap-3 xl:grid-cols-2">
      <Card v-for="item in applications" :key="item.id">
        <CardHeader>
          <div class="flex flex-wrap items-start justify-between gap-2">
            <div class="min-w-0">
              <CardTitle class="break-words text-base">{{ item.siteName }}</CardTitle>
              <CardDescription class="mt-1 break-all">{{ item.siteUrl }}</CardDescription>
            </div>
            <Badge :variant="statusVariant(item.status)">{{ statusText(item.status) }}</Badge>
          </div>
        </CardHeader>
        <CardContent class="flex flex-col gap-3 text-sm">
          <div class="grid gap-2 sm:grid-cols-2">
            <div>
              <span class="text-muted-foreground">联系邮箱：</span>
              <span class="break-all">{{ item.applicantEmail }}</span>
            </div>
            <div>
              <span class="text-muted-foreground">提交时间：</span>
              <span>{{ formatDateTime(item.createTime) }}</span>
            </div>
          </div>
          <p class="whitespace-pre-wrap break-words leading-6 text-muted-foreground">
            {{ item.description || '未填写网站简介' }}
          </p>
          <a
            :href="item.siteUrl"
            target="_blank"
            rel="noreferrer"
            class="inline-flex w-fit items-center gap-1 break-all text-primary hover:underline"
          >
            访问申请站点
            <ExternalLink :size="14" />
          </a>

          <label v-if="item.status === 0" class="flex flex-col gap-1">
            审核备注
            <Textarea
              v-model="remarks[item.id]"
              :disabled="loading || processingId !== undefined"
              maxlength="500"
              placeholder="驳回时必填；通过时可记录检查结果"
            />
          </label>
          <div v-else class="rounded-md border bg-surface p-3">
            <div class="text-xs text-muted-foreground">审核备注</div>
            <p class="mt-1 whitespace-pre-wrap break-words leading-6">{{ item.auditRemark || '无' }}</p>
            <div class="mt-2 text-xs text-muted-foreground">
              审核时间：{{ formatDateTime(item.auditTime) }}
            </div>
          </div>

          <div class="flex flex-wrap gap-2">
            <template v-if="item.status === 0">
              <Button :disabled="loading || processingId !== undefined" @click="auditApplication(item, 1)">
                <LoaderCircle v-if="processingId === item.id" class="animate-spin" />
                <Check v-else />
                通过
              </Button>
              <Button
                variant="outline"
                :disabled="loading || processingId !== undefined"
                @click="auditApplication(item, 2)"
              >
                <X />
                驳回
              </Button>
            </template>
            <Button
              variant="ghost"
              :disabled="loading || processingId !== undefined"
              @click="removeApplication(item)"
            >
              <Trash2 />
              删除
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>

    <div
      v-if="!initialLoading && !loading && !error && applications.length === 0"
      class="rounded-md border p-10 text-center text-sm text-muted-foreground"
    >
      当前筛选条件下暂无友链申请
    </div>

    <PublicPagination
      v-if="!initialLoading && !error && total > 0"
      :page-no="pageNo"
      :total-pages="totalPages"
      :total="total"
      :loading="loading"
      @previous="previousPage"
      @next="nextPage"
    />
  </div>
</template>
