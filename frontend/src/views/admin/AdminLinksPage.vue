<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { createLinkApi, deleteLinkApi, listAdminLinksApi, updateLinkApi, type LinkItem, type LinkPayload } from '../../api/link'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'
import { Textarea } from '../../components/ui/textarea'

const links = ref<LinkItem[]>([])
const editingId = ref<number>()
const loading = ref(false)
const message = ref('')
const error = ref('')

const form = reactive<LinkPayload>({
  siteName: '',
  siteUrl: '',
  iconUrl: '',
  description: '',
  sortOrder: 0,
  status: 1,
})

const resetForm = () => {
  editingId.value = undefined
  form.siteName = ''
  form.siteUrl = ''
  form.iconUrl = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
}

const loadLinks = async () => {
  loading.value = true
  try {
    links.value = await listAdminLinksApi()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '友链加载失败'
  } finally {
    loading.value = false
  }
}

const editLink = (item: LinkItem) => {
  editingId.value = item.id
  form.siteName = item.siteName
  form.siteUrl = item.siteUrl
  form.iconUrl = item.iconUrl || ''
  form.description = item.description || ''
  form.sortOrder = item.sortOrder
  form.status = item.status
}

const saveLink = async () => {
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    if (editingId.value) {
      await updateLinkApi(editingId.value, { ...form })
      message.value = '友链已更新'
    } else {
      await createLinkApi({ ...form })
      message.value = '友链已新增'
    }
    resetForm()
    await loadLinks()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

const removeLink = async (id: number) => {
  loading.value = true
  error.value = ''
  try {
    await deleteLinkApi(id)
    message.value = '友链已删除'
    await loadLinks()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '删除失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadLinks)
</script>

<template>
  <div class="grid gap-4 xl:grid-cols-[1fr_360px]">
    <div class="flex flex-col gap-4">
      <div>
        <h1 class="text-xl font-semibold">友链管理</h1>
        <p class="mt-1 text-sm text-muted-foreground">维护前台展示的友情链接，隐藏状态不会在前台出现。</p>
      </div>

      <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
        {{ error || message }}
      </Alert>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>网站</TableHead>
              <TableHead>地址</TableHead>
              <TableHead>排序</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in links" :key="item.id">
              <TableCell>
                <div class="font-medium">{{ item.siteName }}</div>
                <div class="text-xs text-muted-foreground">{{ item.description || '-' }}</div>
              </TableCell>
              <TableCell>
                <a :href="item.siteUrl" target="_blank" rel="noreferrer" class="text-primary">{{ item.siteUrl }}</a>
              </TableCell>
              <TableCell>{{ item.sortOrder }}</TableCell>
              <TableCell>
                <Badge :variant="item.status === 1 ? 'secondary' : 'outline'">
                  {{ item.status === 1 ? '显示' : '隐藏' }}
                </Badge>
              </TableCell>
              <TableCell>
                <div class="flex flex-wrap gap-2">
                  <Button variant="ghost" @click="editLink(item)">编辑</Button>
                  <Button variant="ghost" @click="removeLink(item.id)">删除</Button>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-if="links.length === 0">
              <TableCell colspan="5" class="py-10 text-center text-muted-foreground">暂无友链</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <CardTitle>{{ editingId ? '编辑友链' : '新增友链' }}</CardTitle>
      </CardHeader>
      <CardContent class="flex flex-col gap-3">
        <label class="block text-sm">
          网站名称
          <Input v-model="form.siteName" class="mt-1" />
        </label>
        <label class="block text-sm">
          网站地址
          <Input v-model="form.siteUrl" class="mt-1" placeholder="https://example.com" />
        </label>
        <label class="block text-sm">
          图标地址
          <Input v-model="form.iconUrl" class="mt-1" />
        </label>
        <label class="block text-sm">
          描述
          <Textarea v-model="form.description" class="mt-1" />
        </label>
        <label class="block text-sm">
          排序
          <Input v-model.number="form.sortOrder" type="number" class="mt-1" />
        </label>
        <label class="block text-sm">
          状态
          <Select :model-value="form.status" class="mt-1" @update:model-value="form.status = Number($event)">
            <option :value="1">显示</option>
            <option :value="0">隐藏</option>
          </Select>
        </label>
        <div class="flex gap-2">
          <Button :disabled="loading" @click="saveLink">保存</Button>
          <Button variant="outline" :disabled="loading" @click="resetForm">取消</Button>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
