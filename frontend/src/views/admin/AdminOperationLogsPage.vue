<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { pageOperationLogsApi, type OperationLogItem } from '../../api/operationLog'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'

const logs = ref<OperationLogItem[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = 10
const moduleKeyword = ref('')
const successFlag = ref('')
const loading = ref(false)
const error = ref('')

const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))

const loadLogs = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await pageOperationLogsApi({
      pageNo: pageNo.value,
      pageSize,
      module: moduleKeyword.value,
      successFlag: successFlag.value === '' ? undefined : Number(successFlag.value),
    })
    logs.value = page.records
    total.value = page.total
  } catch (err) {
    error.value = err instanceof Error ? err.message : '操作日志加载失败'
  } finally {
    loading.value = false
  }
}

const search = async () => {
  pageNo.value = 1
  await loadLogs()
}

const resetFilters = async () => {
  moduleKeyword.value = ''
  successFlag.value = ''
  await search()
}

const goPage = async (nextPage: number) => {
  pageNo.value = nextPage
  await loadLogs()
}

const formatParams = (value?: string) => {
  if (!value) {
    return '无参数'
  }
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

onMounted(loadLogs)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div>
      <h1 class="text-xl font-semibold">操作日志</h1>
      <p class="mt-1 text-sm text-muted-foreground">查看后台操作审计记录，敏感字段会在后端写入时脱敏。</p>
    </div>

    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <Card>
      <CardContent class="grid gap-3 p-3 md:grid-cols-[1fr_160px_auto]">
        <Input v-model="moduleKeyword" placeholder="按模块搜索，例如 文章管理" />
        <Select v-model="successFlag">
          <option value="">全部状态</option>
          <option value="1">成功</option>
          <option value="0">失败</option>
        </Select>
        <div class="flex flex-wrap gap-2">
          <Button variant="outline" :disabled="loading" @click="search">搜索</Button>
          <Button variant="ghost" :disabled="loading" @click="resetFilters">重置</Button>
        </div>
      </CardContent>
    </Card>

    <Card v-for="item in logs" :key="item.id">
      <CardHeader class="gap-2">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <CardTitle class="text-base">{{ item.module }} / {{ item.operation }}</CardTitle>
          <Badge :variant="item.successFlag === 1 ? 'secondary' : 'outline'">
            {{ item.successFlag === 1 ? '成功' : '失败' }}
          </Badge>
        </div>
        <div class="flex flex-wrap gap-2 text-xs text-muted-foreground">
          <span>{{ item.requestMethod }}</span>
          <span class="max-w-full truncate">{{ item.requestUri }}</span>
          <span>用户 {{ item.userId || '-' }}</span>
          <span>{{ item.ip || '-' }}</span>
          <span>{{ item.createTime || '-' }}</span>
        </div>
      </CardHeader>
      <CardContent class="flex flex-col gap-3">
        <div v-if="item.errorMessage" class="rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
          {{ item.errorMessage }}
        </div>
        <div class="min-w-0">
          <div class="mb-1 text-xs text-muted-foreground">请求参数</div>
          <pre class="max-h-56 overflow-auto rounded-md border bg-surface p-3 text-xs leading-5 text-foreground">{{ formatParams(item.requestParams) }}</pre>
        </div>
        <div class="min-w-0 text-xs text-muted-foreground">
          <span class="font-medium text-foreground">User-Agent：</span>
          <span class="break-all">{{ item.userAgent || '-' }}</span>
        </div>
      </CardContent>
    </Card>

    <Card v-if="!loading && logs.length === 0">
      <CardContent class="p-8 text-center text-sm text-muted-foreground">暂无操作日志</CardContent>
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
