<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  getPublicArchiveHeatmapApi,
  listPublicArchivesApi,
  type ArchiveHeatmap,
  type ArchiveMonth,
} from '../../api/publicArticle'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Select } from '../../components/ui/select'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'

const archives = ref<ArchiveMonth[]>([])
const heatmap = ref<ArchiveHeatmap>()
const selectedYear = ref(new Date().getFullYear())
const loadingArchives = ref(true)
const loadingHeatmap = ref(true)
const error = ref('')
const heatmapError = ref('')

const monthLabels = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const archiveYears = computed(() => {
  const years = new Set<number>([new Date().getFullYear()])
  for (const group of archives.value) {
    const year = Number(group.month.slice(0, 4))
    if (!Number.isNaN(year)) {
      years.add(year)
    }
  }
  return [...years].sort((a, b) => b - a)
})

const heatmapCountMap = computed(() => {
  const map = new Map<string, number>()
  for (const day of heatmap.value?.days || []) {
    map.set(day.date, day.count)
  }
  return map
})

const daysInMonth = (year: number, month: number) => new Date(year, month, 0).getDate()

const formatDate = (year: number, month: number, day: number) =>
  `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`

const heatmapRows = computed(() =>
  monthLabels.map((label, index) => {
    const month = index + 1
    const maxDay = daysInMonth(selectedYear.value, month)
    return {
      label,
      days: Array.from({ length: 31 }, (_item, dayIndex) => {
        const day = dayIndex + 1
        if (day > maxDay) {
          return { date: '', day, count: undefined }
        }
        const date = formatDate(selectedYear.value, month, day)
        return { date, day, count: heatmapCountMap.value.get(date) || 0 }
      }),
    }
  }),
)

const heatmapCellClass = (count?: number) => {
  if (count === undefined) {
    return 'invisible'
  }
  if (count >= 4) {
    return 'border-primary bg-primary'
  }
  if (count >= 2) {
    return 'border-primary bg-primary/70'
  }
  if (count === 1) {
    return 'border-primary bg-primary/30'
  }
  return 'border-border bg-background'
}

const loadArchives = async () => {
  loadingArchives.value = true
  error.value = ''
  try {
    archives.value = await listPublicArchivesApi()
    if (!archiveYears.value.includes(selectedYear.value) && archiveYears.value.length > 0) {
      selectedYear.value = archiveYears.value[0]
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '归档加载失败'
    archives.value = []
  } finally {
    loadingArchives.value = false
  }
}

const loadHeatmap = async () => {
  loadingHeatmap.value = true
  heatmapError.value = ''
  heatmap.value = undefined
  try {
    heatmap.value = await getPublicArchiveHeatmapApi(selectedYear.value)
  } catch (err) {
    heatmapError.value = err instanceof Error ? err.message : '写作热力图加载失败'
  } finally {
    loadingHeatmap.value = false
  }
}

const changeYear = async (value: string | number) => {
  selectedYear.value = Number(value)
  await loadHeatmap()
}

onMounted(() => {
  loadArchives()
  loadHeatmap()
})
</script>

<template>
  <div class="flex flex-col gap-4">
    <Card>
      <CardHeader>
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div class="min-w-0">
            <CardTitle>文章归档</CardTitle>
            <p class="mt-1 break-words text-sm text-muted-foreground">按发布时间整理所有公开文章，并展示年度写作节奏。</p>
          </div>
          <Select :model-value="selectedYear" class="w-32" :disabled="loadingHeatmap" @update:model-value="changeYear">
            <option v-for="year in archiveYears" :key="year" :value="year">{{ year }} 年</option>
          </Select>
        </div>
      </CardHeader>
    </Card>

    <Card>
      <CardHeader>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <CardTitle class="text-lg">年度写作热力图</CardTitle>
            <p class="mt-1 text-sm text-muted-foreground">
              {{ selectedYear }} 年已发布 {{ heatmap?.total || 0 }} 篇公开文章。
            </p>
          </div>
          <div class="flex items-center gap-2 text-xs text-muted-foreground">
            <span>少</span>
            <span class="size-3 rounded-sm border border-border bg-background"></span>
            <span class="size-3 rounded-sm border border-primary bg-primary/30"></span>
            <span class="size-3 rounded-sm border border-primary bg-primary/70"></span>
            <span class="size-3 rounded-sm border border-primary bg-primary"></span>
            <span>多</span>
          </div>
        </div>
      </CardHeader>
      <CardContent class="flex flex-col gap-2 overflow-x-auto">
        <LoadingState v-if="loadingHeatmap" title="正在加载写作热力图" />
        <Alert v-else-if="heatmapError" variant="destructive">{{ heatmapError }}</Alert>
        <div v-else class="min-w-[760px]">
          <div class="grid grid-cols-[44px_repeat(31,1fr)] gap-1 text-xs text-muted-foreground">
            <div></div>
            <div v-for="day in 31" :key="day" class="text-center">{{ day }}</div>
          </div>
          <div class="mt-2 flex flex-col gap-1">
            <div
              v-for="row in heatmapRows"
              :key="row.label"
              class="grid grid-cols-[44px_repeat(31,1fr)] items-center gap-1"
            >
              <div class="text-xs text-muted-foreground">{{ row.label }}</div>
              <span
                v-for="day in row.days"
                :key="`${row.label}-${day.day}`"
                class="size-4 rounded-sm border"
                :class="heatmapCellClass(day.count)"
                :title="day.count === undefined ? '' : `${day.date} 发布 ${day.count} 篇文章`"
              ></span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <LoadingState
      v-if="loadingArchives"
      title="正在加载文章归档"
      description="正在按发布时间整理公开文章。"
    />

    <Card v-for="group in archives" :key="group.month">
      <CardHeader>
        <CardTitle class="text-lg">{{ group.monthLabel }}</CardTitle>
      </CardHeader>
      <CardContent class="flex flex-col gap-3">
        <RouterLink
          v-for="article in group.articles"
          :key="article.id"
          :to="`/articles/${article.slug}`"
          class="flex flex-wrap items-center justify-between gap-2 rounded-md border p-3 hover:bg-hover"
        >
          <span class="min-w-0 break-words font-medium">{{ article.title }}</span>
          <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <Badge v-if="article.topFlag === 1">置顶</Badge>
            <span>{{ article.categoryName || '未分类' }}</span>
            <span>{{ article.viewCount }} 阅读</span>
          </div>
        </RouterLink>
      </CardContent>
    </Card>

    <EmptyState
      v-if="!loadingArchives && !error && archives.length === 0"
      title="暂无公开归档"
      description="当前还没有可归档的公开文章，可以先浏览首页或查看已有专栏。"
      action-label="返回首页"
      action-to="/"
      secondary-label="查看专栏"
      secondary-to="/columns"
    />
  </div>
</template>
