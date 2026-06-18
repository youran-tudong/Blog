<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginApi } from '../../api/auth'
import { Alert } from '../../components/ui/alert'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { useAppStore } from '../../stores/appStore'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const username = ref('admin')
const password = ref('')
const loading = ref(false)
const error = ref('')
const message = ref(route.query.passwordChanged === '1' ? '密码已修改，请使用新密码重新登录' : '')

const handleSubmit = async () => {
  error.value = ''
  message.value = ''
  if (!username.value.trim() || !password.value.trim()) {
    error.value = '请输入用户名和密码'
    return
  }
  try {
    loading.value = true
    const resp = await loginApi({ username: username.value.trim(), password: password.value })
    appStore.setToken(resp.tokenValue)
    appStore.setUser(resp.userInfo)
    await router.replace('/admin')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-background px-4">
    <Card class="w-full max-w-sm">
      <CardHeader>
        <CardTitle>TechNote 登录</CardTitle>
      </CardHeader>
      <CardContent>
        <form class="flex flex-col gap-4" @submit.prevent="handleSubmit">
          <label class="block text-sm">
            用户名
            <Input v-model="username" class="mt-1" />
          </label>
          <label class="block text-sm">
            密码
            <Input v-model="password" type="password" class="mt-1" />
          </label>
          <Alert v-if="error" variant="destructive">
            {{ error }}
          </Alert>
          <Alert v-else-if="message" variant="success">
            {{ message }}
          </Alert>
          <Button class="w-full" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
