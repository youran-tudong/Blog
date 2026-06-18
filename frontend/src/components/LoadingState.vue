<script setup lang="ts">
import { LoaderCircle } from 'lucide-vue-next'

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
  }>(),
  {
    title: '正在加载',
  },
)
</script>

<template>
  <div
    role="status"
    aria-live="polite"
    class="flex min-h-28 flex-col items-center justify-center gap-2 p-6 text-center"
  >
    <LoaderCircle class="animate-spin text-primary" :size="20" />
    <div class="break-words text-sm font-medium">{{ props.title }}</div>
    <p v-if="props.description" class="max-w-md break-words text-xs leading-5 text-muted-foreground">
      {{ props.description }}
    </p>
  </div>
</template>
