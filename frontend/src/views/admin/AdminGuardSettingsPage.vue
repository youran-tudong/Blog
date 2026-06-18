<script setup lang="ts">
import { LoaderCircle, Save, ShieldCheck } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import {
  getPublicSubmitGuardSettingApi,
  updatePublicSubmitGuardSettingApi,
  type PublicSubmitGuardSettingPayload,
} from '../../api/guard'
import AdminPageHeader from '../../components/admin/AdminPageHeader.vue'
import AdminSettingsSection from '../../components/admin/AdminSettingsSection.vue'
import { Alert } from '../../components/ui/alert'
import { Button } from '../../components/ui/button'
import { Checkbox } from '../../components/ui/checkbox'
import { Input } from '../../components/ui/input'
import { Textarea } from '../../components/ui/textarea'

const loading = ref(true)
const saving = ref(false)
const message = ref('')
const error = ref('')

const form = reactive<PublicSubmitGuardSettingPayload>({
  enabled: true,
  captchaEnabled: true,
  captchaTtlSeconds: 300,
  captchaMaxEntries: 2000,
  sensitiveWordEnabled: true,
  sensitiveWordMessage: '',
  sensitiveWords: '',
})

const fillForm = (data: PublicSubmitGuardSettingPayload) => {
  form.enabled = data.enabled
  form.captchaEnabled = data.captchaEnabled
  form.captchaTtlSeconds = data.captchaTtlSeconds
  form.captchaMaxEntries = data.captchaMaxEntries
  form.sensitiveWordEnabled = data.sensitiveWordEnabled
  form.sensitiveWordMessage = data.sensitiveWordMessage || ''
  form.sensitiveWords = data.sensitiveWords || ''
}

const loadSetting = async () => {
  loading.value = true
  error.value = ''
  try {
    fillForm(await getPublicSubmitGuardSettingApi())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '风控设置加载失败'
  } finally {
    loading.value = false
  }
}

const saveSetting = async () => {
  message.value = ''
  error.value = ''
  if (form.captchaTtlSeconds < 60 || form.captchaTtlSeconds > 3600) {
    error.value = '验证码有效期需要在 60 到 3600 秒之间'
    return
  }
  if (form.captchaMaxEntries < 100 || form.captchaMaxEntries > 20000) {
    error.value = '验证码保留数量需要在 100 到 20000 之间'
    return
  }
  saving.value = true
  try {
    fillForm(await updatePublicSubmitGuardSettingApi({ ...form }))
    message.value = '公开提交风控设置已保存'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '风控设置保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(loadSetting)
</script>

<template>
  <div class="flex flex-col gap-4">
    <AdminPageHeader title="公开风控" description="维护评论、留言和友链申请的验证码与敏感词策略。">
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

    <Alert v-if="loading">正在加载公开提交风控设置</Alert>

    <div v-else class="grid gap-4 xl:grid-cols-[1fr_1fr]">
      <AdminSettingsSection title="基础策略" description="关闭总开关后，验证码和敏感词检查都会跳过。">
        <template #title-prefix>
          <ShieldCheck />
        </template>
        <label class="flex items-center gap-3 text-sm">
          <Checkbox v-model:checked="form.enabled" />
          启用公开提交防护
        </label>
        <label class="flex items-center gap-3 text-sm">
          <Checkbox v-model:checked="form.captchaEnabled" :disabled="!form.enabled" />
          启用数学验证码
        </label>
        <div class="grid gap-3 sm:grid-cols-2">
          <label class="flex flex-col gap-1 text-sm">
            验证码有效期（秒）
            <Input
              v-model.number="form.captchaTtlSeconds"
              type="number"
              min="60"
              max="3600"
              :disabled="!form.enabled || !form.captchaEnabled"
            />
          </label>
          <label class="flex flex-col gap-1 text-sm">
            最大保留数量
            <Input
              v-model.number="form.captchaMaxEntries"
              type="number"
              min="100"
              max="20000"
              :disabled="!form.enabled || !form.captchaEnabled"
            />
          </label>
        </div>
      </AdminSettingsSection>

      <AdminSettingsSection title="敏感词过滤" description="按行维护敏感词，命中后公开提交会被拒绝。">
        <label class="flex items-center gap-3 text-sm">
          <Checkbox v-model:checked="form.sensitiveWordEnabled" :disabled="!form.enabled" />
          启用敏感词过滤
        </label>
        <label class="flex flex-col gap-1 text-sm">
          命中提示
          <Input
            v-model="form.sensitiveWordMessage"
            maxlength="120"
            :disabled="!form.enabled || !form.sensitiveWordEnabled"
            placeholder="提交内容包含暂不支持发布的词汇，请调整后再提交"
          />
        </label>
        <label class="flex flex-col gap-1 text-sm">
          敏感词列表
          <Textarea
            v-model="form.sensitiveWords"
            class="min-h-[260px]"
            maxlength="4000"
            :disabled="!form.enabled || !form.sensitiveWordEnabled"
            placeholder="每行一个词，例如：spam"
          />
        </label>
      </AdminSettingsSection>
    </div>
  </div>
</template>
