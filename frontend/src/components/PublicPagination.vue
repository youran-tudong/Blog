<script setup lang="ts">
import { computed } from 'vue'
import { Button } from './ui/button'

const props = withDefaults(
  defineProps<{
    pageNo: number
    totalPages: number
    total?: number
    loading?: boolean
  }>(),
  {
    loading: false,
  },
)

const emit = defineEmits<{
  previous: []
  next: []
}>()

const canPrevious = computed(() => props.pageNo > 1 && !props.loading)
const canNext = computed(() => props.pageNo < props.totalPages && !props.loading)
</script>

<template>
  <nav
    aria-label="分页导航"
    class="flex flex-col gap-2 text-sm text-muted-foreground sm:flex-row sm:items-center sm:justify-between"
  >
    <span class="break-words">
      第 {{ props.pageNo }} / {{ props.totalPages }} 页<template v-if="props.total !== undefined">，共 {{ props.total }} 条</template>
    </span>
    <div class="grid grid-cols-2 gap-2 sm:flex">
      <Button class="w-full sm:w-auto" variant="outline" :disabled="!canPrevious" @click="emit('previous')">
        上一页
      </Button>
      <Button class="w-full sm:w-auto" variant="outline" :disabled="!canNext" @click="emit('next')">
        下一页
      </Button>
    </div>
  </nav>
</template>
