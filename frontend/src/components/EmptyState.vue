<script setup lang="ts">
import { Inbox } from 'lucide-vue-next'
import { RouterLink, type RouteLocationRaw } from 'vue-router'
import { Card, CardContent } from './ui/card'
import { buttonVariants } from './ui/button'

const props = defineProps<{
  title: string
  description?: string
  actionLabel?: string
  actionTo?: RouteLocationRaw
  secondaryLabel?: string
  secondaryTo?: RouteLocationRaw
}>()
</script>

<template>
  <Card>
    <CardContent class="flex flex-col items-center gap-3 p-8 text-center">
      <div class="flex size-9 items-center justify-center rounded-md border bg-surface text-muted-foreground">
        <Inbox :size="16" />
      </div>
      <div class="break-words text-base font-semibold">{{ props.title }}</div>
      <p v-if="props.description" class="max-w-md break-words text-sm leading-6 text-muted-foreground">
        {{ props.description }}
      </p>
      <div v-if="props.actionLabel || props.secondaryLabel" class="flex flex-wrap justify-center gap-2">
        <RouterLink v-if="props.actionLabel && props.actionTo" :to="props.actionTo || '/'" :class="buttonVariants()">
          {{ props.actionLabel }}
        </RouterLink>
        <RouterLink
          v-if="props.secondaryLabel && props.secondaryTo"
          :to="props.secondaryTo || '/'"
          :class="buttonVariants({ variant: 'outline' })"
        >
          {{ props.secondaryLabel }}
        </RouterLink>
      </div>
    </CardContent>
  </Card>
</template>
