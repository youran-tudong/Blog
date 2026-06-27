<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getPublicSettingApi, type PublicSettingItem } from '../../api/setting'
import { Alert } from '../../components/ui/alert'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import LoadingState from '../../components/LoadingState.vue'
import { renderMarkdown } from '../../lib/markdown'

const setting = ref<PublicSettingItem>()
const loading = ref(true)
const error = ref('')
const aboutHtml = computed(() => renderMarkdown(setting.value?.aboutContent || '欢迎来到 TechNote。'))

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    setting.value = await getPublicSettingApi()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '关于页面加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>关于 {{ setting?.siteTitle || 'TechNote' }}</CardTitle>
      <p v-if="setting?.siteDescription" class="text-sm leading-6 text-muted-foreground">
        {{ setting.siteDescription }}
      </p>
    </CardHeader>
    <CardContent>
      <Alert v-if="error" variant="destructive">{{ error }}</Alert>
      <LoadingState v-else-if="loading" title="正在加载关于页面" />
      <article
        v-else
        class="flex flex-col gap-3 text-sm leading-7 [overflow-wrap:anywhere] [&_a]:text-primary [&_blockquote]:rounded-md [&_blockquote]:border-l-4 [&_blockquote]:bg-surface [&_blockquote]:p-3 [&_code]:rounded-sm [&_code]:bg-surface [&_code]:px-1 [&_img]:max-h-[420px] [&_img]:w-full [&_img]:rounded-md [&_img]:border [&_img]:object-contain [&_pre]:max-w-full [&_pre]:overflow-auto [&_pre]:rounded-md [&_pre]:border [&_pre]:bg-surface [&_pre]:p-3 [&_ul]:list-disc [&_ul]:pl-5"
        v-html="aboutHtml"
      />
    </CardContent>
  </Card>
</template>
