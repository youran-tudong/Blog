<script setup lang="ts">
import { LoaderCircle, Save } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import { getAdminSettingApi, updateAdminSettingApi, type SettingPayload } from '../../api/setting'
import AdminPageHeader from '../../components/admin/AdminPageHeader.vue'
import AdminSettingsSection from '../../components/admin/AdminSettingsSection.vue'
import { Alert } from '../../components/ui/alert'
import { Button } from '../../components/ui/button'
import { Input } from '../../components/ui/input'
import { Textarea } from '../../components/ui/textarea'

const loading = ref(true)
const saving = ref(false)
const message = ref('')
const error = ref('')

const form = reactive<SettingPayload>({
  siteTitle: '',
  siteDescription: '',
  siteKeywords: '',
  icpNo: '',
  authorName: '',
  authorAvatar: '',
  authorProfile: '',
  announcement: '',
  aboutContent: '',
})

const fillForm = (data: SettingPayload) => {
  form.siteTitle = data.siteTitle || ''
  form.siteDescription = data.siteDescription || ''
  form.siteKeywords = data.siteKeywords || ''
  form.icpNo = data.icpNo || ''
  form.authorName = data.authorName || ''
  form.authorAvatar = data.authorAvatar || ''
  form.authorProfile = data.authorProfile || ''
  form.announcement = data.announcement || ''
  form.aboutContent = data.aboutContent || ''
}

const loadSetting = async () => {
  loading.value = true
  try {
    fillForm(await getAdminSettingApi())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '设置加载失败'
  } finally {
    loading.value = false
  }
}

const saveSetting = async () => {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    fillForm(await updateAdminSettingApi({ ...form }))
    message.value = '系统设置已保存'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(loadSetting)
</script>

<template>
  <div class="flex flex-col gap-4">
    <AdminPageHeader title="系统设置" description="维护前台站点标题、作者信息、公告和关于页面内容。">
      <template #actions>
        <Button :disabled="loading || saving" @click="saveSetting">
          <LoaderCircle v-if="saving" class="animate-spin" data-icon="inline-start" />
          <Save v-else data-icon="inline-start" />
          保存设置
        </Button>
      </template>
    </AdminPageHeader>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <Alert v-if="loading">正在加载系统设置</Alert>

    <div v-else class="grid gap-4 xl:grid-cols-[1fr_1fr]">
      <AdminSettingsSection title="站点信息">
        <div class="flex flex-col gap-3">
          <label class="block text-sm">
            站点标题
            <Input v-model="form.siteTitle" class="mt-1" />
          </label>
          <label class="block text-sm">
            站点简介
            <Textarea v-model="form.siteDescription" class="mt-1" />
          </label>
          <label class="block text-sm">
            关键词
            <Input v-model="form.siteKeywords" class="mt-1" />
          </label>
          <label class="block text-sm">
            备案号
            <Input v-model="form.icpNo" class="mt-1" />
          </label>
          <label class="block text-sm">
            公告
            <Textarea v-model="form.announcement" class="mt-1" />
          </label>
        </div>
      </AdminSettingsSection>

      <AdminSettingsSection title="作者信息">
        <div class="flex flex-col gap-3">
          <label class="block text-sm">
            博主名称
            <Input v-model="form.authorName" class="mt-1" />
          </label>
          <label class="block text-sm">
            头像地址
            <Input v-model="form.authorAvatar" class="mt-1" />
          </label>
          <label class="block text-sm">
            博主介绍
            <Textarea v-model="form.authorProfile" class="mt-1 min-h-[120px]" />
          </label>
          <label class="block text-sm">
            关于页面内容
            <Textarea v-model="form.aboutContent" class="mt-1 min-h-[220px]" />
          </label>
        </div>
      </AdminSettingsSection>
    </div>
  </div>
</template>
