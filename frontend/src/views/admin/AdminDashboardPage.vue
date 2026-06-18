<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { computed, onMounted, ref } from 'vue'
import { getDashboardStatsApi, type DashboardStats } from '../../api/dashboard'
import DashboardChart from '../../components/DashboardChart.vue'
import LoadingState from '../../components/LoadingState.vue'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'
import { useAppStore } from '../../stores/appStore'

const appStore = useAppStore()
const stats = ref<DashboardStats>()
const loading = ref(true)
const error = ref('')
const trendPeriod = ref<7 | 30>(7)

const numberFormatter = new Intl.NumberFormat('zh-CN')

const formatNumber = (value?: number) => numberFormatter.format(value ?? 0)

const formatDateTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ').slice(0, 19)
}

const statusText = (value: number) => {
  if (value === 1) return '发布'
  if (value === 2) return '自动草稿'
  return '草稿'
}

const metrics = computed(() => [
  { label: '文章总数', value: formatNumber(stats.value?.articleTotal), hint: '含草稿与已发布内容' },
  { label: '已发布', value: formatNumber(stats.value?.publishedArticleCount), hint: '处于已发布状态的内容' },
  { label: '草稿', value: formatNumber(stats.value?.draftArticleCount), hint: '等待继续编辑的文章' },
  { label: '自动草稿', value: formatNumber(stats.value?.autoDraftCount), hint: '编辑器自动保存快照' },
  { label: '总阅读量', value: formatNumber(stats.value?.viewTotal), hint: '未删除文章阅读汇总' },
  { label: '分类 / 标签', value: `${formatNumber(stats.value?.categoryCount)} / ${formatNumber(stats.value?.tagCount)}`, hint: '内容组织基础数据' },
  { label: '专栏', value: formatNumber(stats.value?.columnCount), hint: '专题化内容集合' },
  {
    label: '待审互动',
    value: formatNumber((stats.value?.pendingCommentCount ?? 0) + (stats.value?.pendingGuestbookCount ?? 0)),
    hint: '评论与留言待处理量',
  },
])

const chartColors = () => {
  void appStore.theme
  const style = getComputedStyle(document.documentElement)
  const token = (name: string) => `hsl(${style.getPropertyValue(name).trim()})`
  return {
    primary: token('--primary'),
    foreground: token('--foreground'),
    mutedForeground: token('--muted-foreground'),
    border: token('--border'),
    surface: token('--surface'),
  }
}

const visibleTrendDays = computed(() => (stats.value?.trendDays || []).slice(-trendPeriod.value))

const trendChartOption = computed<EChartsOption>(() => {
  const colors = chartColors()
  return {
    color: [colors.primary, colors.mutedForeground],
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['阅读量', '发布量'],
      textStyle: { color: colors.mutedForeground },
    },
    grid: { left: 42, right: 16, top: 42, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: visibleTrendDays.value.map((item) => item.date.slice(5)),
      axisLine: { lineStyle: { color: colors.border } },
      axisTick: { show: false },
      axisLabel: { color: colors.mutedForeground },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: colors.mutedForeground },
      splitLine: { lineStyle: { color: colors.border } },
    },
    series: [
      {
        name: '阅读量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: visibleTrendDays.value.map((item) => item.viewCount),
        areaStyle: { color: colors.surface, opacity: 0.55 },
      },
      {
        name: '发布量',
        type: 'bar',
        barMaxWidth: 16,
        data: visibleTrendDays.value.map((item) => item.publishCount),
      },
    ],
  }
})

const categoryChartData = computed(() => {
  const data = stats.value?.categoryDistribution || []
  if (data.length <= 7) return data
  const primary = data.slice(0, 6)
  return [
    ...primary,
    {
      categoryId: undefined,
      categoryName: '其他',
      articleCount: data.slice(6).reduce((total, item) => total + item.articleCount, 0),
    },
  ]
})

const categoryTotal = computed(() =>
  categoryChartData.value.reduce((total, item) => total + item.articleCount, 0),
)

const categoryChartOption = computed<EChartsOption>(() => {
  const colors = chartColors()
  return {
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 篇（{d}%）' },
    series: [
      {
        name: '分类内容占比',
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['50%', '48%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: colors.surface, borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, color: colors.foreground, fontWeight: 600 } },
        data: categoryChartData.value.map((item) => ({
          name: item.categoryName,
          value: item.articleCount,
        })),
      },
    ],
  }
})

const categoryPercent = (count: number) => {
  if (categoryTotal.value === 0) return '0%'
  return `${((count / categoryTotal.value) * 100).toFixed(1)}%`
}

const loadStats = async () => {
  loading.value = true
  error.value = ''
  try {
    stats.value = await getDashboardStatsApi()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '数据概览加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-xl font-semibold">数据概览</h1>
        <p class="mt-1 text-sm text-muted-foreground">查看内容、互动、素材和后台操作的实时概况。</p>
      </div>
      <Button variant="outline" :disabled="loading" @click="loadStats">
        {{ loading ? '刷新中' : '刷新' }}
      </Button>
    </div>

    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <Card v-for="item in metrics" :key="item.label">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm text-muted-foreground">{{ item.label }}</CardTitle>
          <CardDescription>{{ item.hint }}</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-semibold">{{ item.value }}</div>
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-4 xl:grid-cols-[1.35fr_1fr]">
      <Card>
        <CardHeader class="flex-row flex-wrap items-start justify-between gap-3">
          <div>
            <CardTitle>阅读与发布趋势</CardTitle>
            <CardDescription>阅读量从每日聚合数据统计，发布量按文章发布时间统计。</CardDescription>
          </div>
          <div class="flex rounded-md border p-0.5" aria-label="趋势时间范围">
            <Button
              size="sm"
              :variant="trendPeriod === 7 ? 'secondary' : 'ghost'"
              :aria-pressed="trendPeriod === 7"
              @click="trendPeriod = 7"
            >
              近 7 天
            </Button>
            <Button
              size="sm"
              :variant="trendPeriod === 30 ? 'secondary' : 'ghost'"
              :aria-pressed="trendPeriod === 30"
              @click="trendPeriod = 30"
            >
              近 30 天
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <LoadingState v-if="loading && !stats" title="正在加载趋势数据" />
          <div v-else-if="!stats" class="p-8 text-center text-sm text-muted-foreground">趋势数据暂不可用</div>
          <DashboardChart
            v-else
            :option="trendChartOption"
            :aria-label="`近 ${trendPeriod} 天阅读量与发布量趋势图`"
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>分类内容占比</CardTitle>
          <CardDescription>统计当前全部未删除文章，较小分类合并为其他。</CardDescription>
        </CardHeader>
        <CardContent>
          <LoadingState v-if="loading && !stats" title="正在加载分类分布" />
          <div v-else-if="!stats" class="p-8 text-center text-sm text-muted-foreground">分类分布暂不可用</div>
          <div v-else-if="categoryChartData.length > 0" class="grid gap-3 sm:grid-cols-[minmax(0,1fr)_180px] xl:grid-cols-1">
            <DashboardChart :option="categoryChartOption" aria-label="文章分类内容占比环形图" height-class="h-60" />
            <div class="flex flex-col gap-2">
              <div
                v-for="item in categoryChartData"
                :key="`${item.categoryId || 'other'}-${item.categoryName}`"
                class="flex items-center justify-between gap-3 text-sm"
              >
                <span class="min-w-0 truncate" :title="item.categoryName">{{ item.categoryName }}</span>
                <span class="flex shrink-0 items-center gap-2 text-muted-foreground">
                  {{ formatNumber(item.articleCount) }} 篇
                  <Badge variant="outline">{{ categoryPercent(item.articleCount) }}</Badge>
                </span>
              </div>
            </div>
          </div>
          <div v-else class="p-8 text-center text-sm text-muted-foreground">暂无文章分类数据</div>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <CardTitle>热门公开文章</CardTitle>
        <CardDescription>按阅读量展示当前最受关注的 5 篇公开文章。</CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="w-16">排行</TableHead>
              <TableHead>标题</TableHead>
              <TableHead>阅读量</TableHead>
              <TableHead>发布时间</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="(item, index) in stats?.popularArticles || []" :key="item.id">
              <TableCell>
                <Badge variant="outline">{{ index + 1 }}</Badge>
              </TableCell>
              <TableCell>
                <div class="min-w-0">
                  <div class="truncate font-medium">{{ item.title }}</div>
                  <div class="truncate text-xs text-muted-foreground">{{ item.slug }}</div>
                </div>
              </TableCell>
              <TableCell>{{ formatNumber(item.viewCount) }}</TableCell>
              <TableCell>{{ formatDateTime(item.publishTime) }}</TableCell>
            </TableRow>
            <TableRow v-if="(stats?.popularArticles || []).length === 0">
              <TableCell colspan="4" class="py-10 text-center text-muted-foreground">
                {{ loading ? '正在加载热门排行' : '暂无公开文章排行' }}
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <div class="grid gap-4 xl:grid-cols-[1.2fr_1fr]">
      <Card>
        <CardHeader>
          <CardTitle>最近更新文章</CardTitle>
          <CardDescription>按更新时间展示最近 5 篇内容。</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>标题</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>阅读</TableHead>
                <TableHead>更新时间</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="item in stats?.recentArticles || []" :key="item.id">
                <TableCell>
                  <div class="min-w-0">
                    <div class="truncate font-medium">{{ item.title }}</div>
                    <div class="truncate text-xs text-muted-foreground">{{ item.slug }}</div>
                  </div>
                </TableCell>
                <TableCell>
                  <div class="flex flex-wrap gap-1">
                    <Badge :variant="item.status === 1 ? 'secondary' : 'outline'">{{ statusText(item.status) }}</Badge>
                    <Badge :variant="item.visibility === 1 ? 'secondary' : 'outline'">
                      {{ item.visibility === 1 ? '公开' : '私密' }}
                    </Badge>
                  </div>
                </TableCell>
                <TableCell>{{ formatNumber(item.viewCount) }}</TableCell>
                <TableCell>{{ formatDateTime(item.updateTime) }}</TableCell>
              </TableRow>
              <TableRow v-if="(stats?.recentArticles || []).length === 0">
                <TableCell colspan="4" class="py-10 text-center text-muted-foreground">
                  {{ loading ? '正在加载文章数据' : '暂无最近文章' }}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>最近后台操作</CardTitle>
          <CardDescription>用于快速发现失败操作和高频管理行为。</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>模块</TableHead>
                <TableHead>操作</TableHead>
                <TableHead>结果</TableHead>
                <TableHead>时间</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="item in stats?.recentOperations || []" :key="item.id">
                <TableCell>{{ item.module || '-' }}</TableCell>
                <TableCell>
                  <div class="max-w-[180px] truncate">{{ item.operation || '-' }}</div>
                </TableCell>
                <TableCell>
                  <Badge :variant="item.successFlag === 1 ? 'secondary' : 'outline'">
                    {{ item.successFlag === 1 ? '成功' : '失败' }}
                  </Badge>
                </TableCell>
                <TableCell>{{ formatDateTime(item.createTime) }}</TableCell>
              </TableRow>
              <TableRow v-if="(stats?.recentOperations || []).length === 0">
                <TableCell colspan="4" class="py-10 text-center text-muted-foreground">
                  {{ loading ? '正在加载操作日志' : '暂无操作日志' }}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
