<script setup lang="ts">
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '../ui/card'

defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle class="flex items-center gap-2">
        <slot name="title-prefix" />
        <span class="min-w-0">{{ title }}</span>
      </CardTitle>
      <CardDescription v-if="description">{{ description }}</CardDescription>
    </CardHeader>
    <CardContent class="flex flex-col gap-4">
      <slot />
    </CardContent>
    <CardFooter v-if="$slots.footer" class="justify-end gap-2">
      <slot name="footer" />
    </CardFooter>
  </Card>
</template>
