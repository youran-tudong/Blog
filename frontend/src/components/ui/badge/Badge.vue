<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(
  defineProps<{
    variant?: 'default' | 'secondary' | 'outline'
    class?: string
  }>(),
  {
    variant: 'default',
  },
)

const classes = computed(() =>
  cn(
    'inline-flex items-center rounded-sm border px-2 py-0.5 text-xs font-medium',
    props.variant === 'default' && 'border-transparent bg-primary text-primary-foreground',
    props.variant === 'secondary' && 'bg-hover text-foreground',
    props.variant === 'outline' && 'bg-background text-foreground',
    props.class,
  ),
)
</script>

<template>
  <span :class="classes">
    <slot />
  </span>
</template>

