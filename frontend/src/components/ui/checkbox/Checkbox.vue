<script setup lang="ts">
import { computed } from 'vue'
import { Check } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const props = defineProps<{
  checked?: boolean
  disabled?: boolean
  class?: string
}>()

const emit = defineEmits<{
  'update:checked': [value: boolean]
}>()

const classes = computed(() =>
  cn(
    'inline-flex size-4 items-center justify-center rounded-sm border bg-background text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 disabled:cursor-not-allowed disabled:opacity-50',
    props.checked && 'border-primary bg-primary text-primary-foreground',
    props.class,
  ),
)
</script>

<template>
  <button
    type="button"
    role="checkbox"
    :aria-checked="checked"
    :disabled="disabled"
    :class="classes"
    @click="emit('update:checked', !checked)"
  >
    <Check v-if="checked" :size="12" />
  </button>
</template>

