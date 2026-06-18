<script setup lang="ts">
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    option: EChartsOption
    ariaLabel: string
    heightClass?: string
  }>(),
  {
    heightClass: 'h-72',
  },
)

const chartRoot = ref<HTMLDivElement>()
let chart: echarts.ECharts | undefined
let resizeObserver: ResizeObserver | undefined

const renderChart = async () => {
  await nextTick()
  if (!chartRoot.value) return
  chart ||= echarts.init(chartRoot.value)
  chart.setOption(props.option, true)
}

onMounted(() => {
  void renderChart()
  if (chartRoot.value) {
    resizeObserver = new ResizeObserver(() => chart?.resize())
    resizeObserver.observe(chartRoot.value)
  }
})

watch(
  () => props.option,
  () => void renderChart(),
  { deep: true },
)

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chart?.dispose()
})
</script>

<template>
  <div ref="chartRoot" role="img" :aria-label="props.ariaLabel" :class="['w-full', props.heightClass]" />
</template>
