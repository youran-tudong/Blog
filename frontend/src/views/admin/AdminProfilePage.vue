<script setup lang="ts">
import { LoaderCircle, LockKeyhole, Save, UserRound } from 'lucide-vue-next'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  getProfileApi,
  updatePasswordApi,
  updateProfileApi,
  type PasswordUpdatePayload,
  type ProfileUpdatePayload,
} from '../../api/profile'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Textarea } from '../../components/ui/textarea'
import { useAppStore } from '../../stores/appStore'

const router = useRouter()
const appStore = useAppStore()
const profileLoading = ref(true)
const profileSaving = ref(false)
const passwordSaving = ref(false)
const avatarBroken = ref(false)
const profileMessage = ref('')
const profileError = ref('')
const passwordError = ref('')

const profileForm = reactive<ProfileUpdatePayload>({
  nickname: '',
  avatarUrl: '',
  email: '',
  bio: '',
})

const passwordForm = reactive<PasswordUpdatePayload>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const fillProfile = (profile: ProfileUpdatePayload) => {
  profileForm.nickname = profile.nickname || ''
  profileForm.avatarUrl = profile.avatarUrl || ''
  profileForm.email = profile.email || ''
  profileForm.bio = profile.bio || ''
}

const loadProfile = async () => {
  profileLoading.value = true
  profileError.value = ''
  try {
    const profile = await getProfileApi()
    fillProfile(profile)
    appStore.setUser(profile)
  } catch (err) {
    profileError.value = err instanceof Error ? err.message : '个人资料加载失败'
  } finally {
    profileLoading.value = false
  }
}

const saveProfile = async () => {
  profileMessage.value = ''
  profileError.value = ''
  if (!profileForm.nickname.trim()) {
    profileError.value = '昵称不能为空'
    return
  }
  profileSaving.value = true
  try {
    const profile = await updateProfileApi({
      nickname: profileForm.nickname.trim(),
      avatarUrl: profileForm.avatarUrl?.trim() || undefined,
      email: profileForm.email?.trim() || undefined,
      bio: profileForm.bio?.trim() || undefined,
    })
    fillProfile(profile)
    appStore.setUser(profile)
    profileMessage.value = '个人资料已保存'
  } catch (err) {
    profileError.value = err instanceof Error ? err.message : '个人资料保存失败'
  } finally {
    profileSaving.value = false
  }
}

const savePassword = async () => {
  passwordError.value = ''
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    passwordError.value = '请完整填写当前密码和新密码'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.newPassword.length < 8 || passwordForm.newPassword.length > 64) {
    passwordError.value = '新密码长度必须在8到64个字符之间'
    return
  }
  if (!/[A-Za-z]/.test(passwordForm.newPassword) || !/\d/.test(passwordForm.newPassword)) {
    passwordError.value = '新密码至少需要包含一个字母和一个数字'
    return
  }
  passwordSaving.value = true
  try {
    await updatePasswordApi({ ...passwordForm })
    appStore.clearAuth()
    await router.replace({ path: '/admin/login', query: { passwordChanged: '1' } })
  } catch (err) {
    passwordError.value = err instanceof Error ? err.message : '密码修改失败'
  } finally {
    passwordSaving.value = false
  }
}

watch(
  () => profileForm.avatarUrl,
  () => {
    avatarBroken.value = false
  },
)

onMounted(loadProfile)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center gap-3">
      <div class="flex size-14 shrink-0 items-center justify-center overflow-hidden rounded-md border bg-surface">
        <img
          v-if="profileForm.avatarUrl && !avatarBroken"
          :src="profileForm.avatarUrl"
          alt="管理员头像"
          class="size-full object-cover"
          @error="avatarBroken = true"
        />
        <UserRound v-else :size="24" class="text-muted-foreground" />
      </div>
      <div class="min-w-0">
        <h1 class="break-words text-xl font-semibold">{{ profileForm.nickname || appStore.user?.nickname || '个人中心' }}</h1>
        <div class="mt-1 flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
          <span>{{ appStore.user?.username || '管理员账号' }}</span>
          <Badge variant="outline">{{ appStore.user?.roleCode || 'ADMIN' }}</Badge>
        </div>
      </div>
    </div>

    <Alert v-if="profileMessage || profileError" :variant="profileError ? 'destructive' : 'success'">
      {{ profileError || profileMessage }}
    </Alert>

    <div class="grid gap-4 xl:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <UserRound :size="18" />
            个人资料
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form class="flex flex-col gap-3" :aria-busy="profileSaving || profileLoading" @submit.prevent="saveProfile">
            <label class="block text-sm">
              登录用户名
              <Input :model-value="appStore.user?.username || ''" class="mt-1" disabled />
            </label>
            <label class="block text-sm">
              显示昵称
              <Input v-model="profileForm.nickname" class="mt-1" :disabled="profileSaving || profileLoading" />
            </label>
            <label class="block text-sm">
              头像地址
              <Input v-model="profileForm.avatarUrl" class="mt-1" :disabled="profileSaving || profileLoading" />
            </label>
            <label class="block text-sm">
              联系邮箱
              <Input v-model="profileForm.email" type="email" class="mt-1" :disabled="profileSaving || profileLoading" />
            </label>
            <label class="block text-sm">
              个人简介
              <Textarea
                v-model="profileForm.bio"
                class="mt-1 min-h-[140px]"
                :disabled="profileSaving || profileLoading"
              />
            </label>
            <Button class="w-fit min-w-24" type="submit" :disabled="profileSaving || profileLoading">
              <LoaderCircle v-if="profileSaving" class="animate-spin" data-icon="inline-start" />
              <Save v-else data-icon="inline-start" />
              {{ profileSaving ? '保存中' : '保存资料' }}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <LockKeyhole :size="18" />
            登录密码
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form class="flex flex-col gap-3" :aria-busy="passwordSaving" @submit.prevent="savePassword">
            <label class="block text-sm">
              当前密码
              <Input
                v-model="passwordForm.oldPassword"
                type="password"
                autocomplete="current-password"
                class="mt-1"
                :disabled="passwordSaving"
              />
            </label>
            <label class="block text-sm">
              新密码
              <Input
                v-model="passwordForm.newPassword"
                type="password"
                autocomplete="new-password"
                class="mt-1"
                :disabled="passwordSaving"
              />
            </label>
            <label class="block text-sm">
              确认新密码
              <Input
                v-model="passwordForm.confirmPassword"
                type="password"
                autocomplete="new-password"
                class="mt-1"
                :disabled="passwordSaving"
              />
            </label>
            <Alert v-if="passwordError" variant="destructive">{{ passwordError }}</Alert>
            <Button class="w-fit min-w-24" type="submit" variant="outline" :disabled="passwordSaving">
              <LoaderCircle v-if="passwordSaving" class="animate-spin" data-icon="inline-start" />
              <LockKeyhole v-else data-icon="inline-start" />
              {{ passwordSaving ? '修改中' : '修改密码' }}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
