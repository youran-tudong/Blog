<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(
  defineProps<{
    variant?: 'default' | 'destructive' | 'success'
    class?: string
  }>(),
  {
    variant: 'default',
  },
)

const classes = computed(() =>
  cn(
    'relative w-full rounded-md border px-3 py-2 text-sm',
    props.variant === 'default' && 'bg-surface text-foreground',
    props.variant === 'destructive' && 'border-red-300 bg-red-50 text-red-700',
    props.variant === 'success' && 'border-green-300 bg-green-50 text-green-700',
    props.class,
  ),
)
</script>

<template>
  <div :class="classes" :role="props.variant === 'destructive' ? 'alert' : 'status'" aria-live="polite">
    <slot />
  </div>
</template>
