<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  createCategoryApi,
  createTagApi,
  deleteCategoryApi,
  deleteTagApi,
  pageCategoriesApi,
  pageTagsApi,
  updateCategoryApi,
  updateTagApi,
} from '../../api/taxonomy'
import type { CategoryItem, CategoryPayload, TagItem, TagPayload } from '../../api/taxonomy'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'
import { Textarea } from '../../components/ui/textarea'

type TaxonomyTab = 'category' | 'tag'

const toSlug = (value: string) => value.trim().toLowerCase().replace(/\s+/g, '-')

const activeTab = ref<TaxonomyTab>('category')
const keyword = ref('')
const categoryPage = ref(1)
const tagPage = ref(1)
const categories = ref<CategoryItem[]>([])
const tags = ref<TagItem[]>([])
const categoryTotal = ref(0)
const tagTotal = ref(0)
const editingCategoryId = ref<number>()
const editingTagId = ref<number>()
const loading = ref(false)
const message = ref('')
const error = ref('')

const categoryForm = reactive<CategoryPayload>({
  name: '',
  slug: '',
  description: '',
  sortOrder: 0,
  status: 1,
})

const tagForm = reactive<TagPayload>({
  name: '',
  slug: '',
  color: '#0969da',
})

const currentPage = computed(() => (activeTab.value === 'category' ? categoryPage.value : tagPage.value))
const total = computed(() => (activeTab.value === 'category' ? categoryTotal.value : tagTotal.value))
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / 10)))
const panelTitle = computed(() => {
  if (activeTab.value === 'category') {
    return editingCategoryId.value ? '编辑分类' : '新增分类'
  }
  return editingTagId.value ? '编辑标签' : '新增标签'
})

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    if (activeTab.value === 'category') {
      const page = await pageCategoriesApi({ pageNo: categoryPage.value, pageSize: 10, keyword: keyword.value })
      categories.value = page.records
      categoryTotal.value = page.total
    } else {
      const page = await pageTagsApi({ pageNo: tagPage.value, pageSize: 10, keyword: keyword.value })
      tags.value = page.records
      tagTotal.value = page.total
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
watch([activeTab, categoryPage, tagPage], loadData)

const resetCategoryForm = () => {
  editingCategoryId.value = undefined
  categoryForm.name = ''
  categoryForm.slug = ''
  categoryForm.description = ''
  categoryForm.sortOrder = 0
  categoryForm.status = 1
}

const resetTagForm = () => {
  editingTagId.value = undefined
  tagForm.name = ''
  tagForm.slug = ''
  tagForm.color = '#0969da'
}

const submitCategory = async () => {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    if (editingCategoryId.value) {
      await updateCategoryApi(editingCategoryId.value, { ...categoryForm })
      message.value = '分类已更新'
    } else {
      await createCategoryApi({ ...categoryForm })
      message.value = '分类已创建'
    }
    resetCategoryForm()
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

const submitTag = async () => {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    if (editingTagId.value) {
      await updateTagApi(editingTagId.value, { ...tagForm })
      message.value = '标签已更新'
    } else {
      await createTagApi({ ...tagForm })
      message.value = '标签已创建'
    }
    resetTagForm()
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

const editCategory = (item: CategoryItem) => {
  editingCategoryId.value = item.id
  categoryForm.name = item.name
  categoryForm.slug = item.slug
  categoryForm.description = item.description || ''
  categoryForm.sortOrder = item.sortOrder
  categoryForm.status = item.status
}

const editTag = (item: TagItem) => {
  editingTagId.value = item.id
  tagForm.name = item.name
  tagForm.slug = item.slug
  tagForm.color = item.color || '#0969da'
}

const removeCategory = async (id: number) => {
  loading.value = true
  error.value = ''
  try {
    await deleteCategoryApi(id)
    message.value = '分类已删除'
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '删除失败'
  } finally {
    loading.value = false
  }
}

const removeTag = async (id: number) => {
  loading.value = true
  error.value = ''
  try {
    await deleteTagApi(id)
    message.value = '标签已删除'
    await loadData()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '删除失败'
  } finally {
    loading.value = false
  }
}

const search = async () => {
  if (activeTab.value === 'category') {
    categoryPage.value = 1
  } else {
    tagPage.value = 1
  }
  await loadData()
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-xl font-semibold">分类标签</h1>
        <p class="mt-1 text-sm text-muted-foreground">管理文章组织结构，前台仅展示启用的分类。</p>
      </div>
      <div class="flex rounded-md border bg-surface p-1">
        <Button
          variant="ghost"
          size="sm"
          class="rounded px-3 py-1.5 text-sm"
          :class="activeTab === 'category' ? 'bg-background text-foreground' : 'text-muted-foreground'"
          @click="activeTab = 'category'"
        >
          分类
        </Button>
        <Button
          variant="ghost"
          size="sm"
          class="rounded px-3 py-1.5 text-sm"
          :class="activeTab === 'tag' ? 'bg-background text-foreground' : 'text-muted-foreground'"
          @click="activeTab = 'tag'"
        >
          标签
        </Button>
      </div>
    </div>

    <Alert
      v-if="message || error"
      :variant="error ? 'destructive' : 'success'"
    >
      {{ error || message }}
    </Alert>

    <div class="grid gap-4 xl:grid-cols-[1fr_340px]">
      <Card class="overflow-hidden">
        <div class="flex flex-wrap items-center gap-2 border-b p-3">
          <Input
            v-model="keyword"
            placeholder="搜索名称或访问标识"
            class="min-w-64"
          />
          <Button variant="outline" :disabled="loading" @click="search">搜索</Button>
        </div>

        <Table v-if="activeTab === 'category'">
          <TableHeader>
            <TableRow>
              <TableHead>名称</TableHead>
              <TableHead>访问标识</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>排序</TableHead>
              <TableHead>文章数</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in categories" :key="item.id">
              <TableCell>{{ item.name }}</TableCell>
              <TableCell class="text-muted-foreground">{{ item.slug }}</TableCell>
              <TableCell>
                <Badge :variant="item.status === 1 ? 'secondary' : 'outline'">
                  {{ item.status === 1 ? '显示' : '隐藏' }}
                </Badge>
              </TableCell>
              <TableCell>{{ item.sortOrder }}</TableCell>
              <TableCell>{{ item.articleCount }}</TableCell>
              <TableCell>
                <div class="flex gap-2">
                  <Button variant="ghost" @click="editCategory(item)">编辑</Button>
                  <Button variant="ghost" :disabled="item.articleCount > 0 || loading" @click="removeCategory(item.id)">
                    删除
                  </Button>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-if="categories.length === 0">
              <TableCell colspan="6" class="py-10 text-center text-muted-foreground">暂无分类</TableCell>
            </TableRow>
          </TableBody>
        </Table>

        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>名称</TableHead>
              <TableHead>访问标识</TableHead>
              <TableHead>颜色</TableHead>
              <TableHead>文章数</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in tags" :key="item.id">
              <TableCell>{{ item.name }}</TableCell>
              <TableCell class="text-muted-foreground">{{ item.slug }}</TableCell>
              <TableCell>
                <span class="inline-flex items-center gap-2">
                  <span class="size-3 rounded-sm border" :style="{ backgroundColor: item.color || '#0969da' }" />
                  {{ item.color || '-' }}
                </span>
              </TableCell>
              <TableCell>{{ item.articleCount }}</TableCell>
              <TableCell>
                <div class="flex gap-2">
                  <Button variant="ghost" @click="editTag(item)">编辑</Button>
                  <Button variant="ghost" :disabled="item.articleCount > 0 || loading" @click="removeTag(item.id)">
                    删除
                  </Button>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-if="tags.length === 0">
              <TableCell colspan="5" class="py-10 text-center text-muted-foreground">暂无标签</TableCell>
            </TableRow>
          </TableBody>
        </Table>

        <div class="flex items-center justify-between p-3 text-sm text-muted-foreground">
          <span>第 {{ currentPage }} / {{ totalPages }} 页，共 {{ total }} 条</span>
          <div class="flex gap-2">
            <Button
              variant="outline"
              :disabled="currentPage <= 1 || loading"
              @click="activeTab === 'category' ? categoryPage-- : tagPage--"
            >
              上一页
            </Button>
            <Button
              variant="outline"
              :disabled="currentPage >= totalPages || loading"
              @click="activeTab === 'category' ? categoryPage++ : tagPage++"
            >
              下一页
            </Button>
          </div>
        </div>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{{ panelTitle }}</CardTitle>
        </CardHeader>
        <CardContent>
          <form v-if="activeTab === 'category'" class="flex flex-col gap-3" @submit.prevent="submitCategory">
            <label class="block text-sm">
              分类名称
              <Input
                v-model="categoryForm.name"
                class="mt-1"
                @input="categoryForm.slug = categoryForm.slug || toSlug(categoryForm.name)"
              />
            </label>
            <label class="block text-sm">
              访问标识
              <Input
                v-model="categoryForm.slug"
                class="mt-1"
                @input="categoryForm.slug = toSlug(categoryForm.slug)"
              />
            </label>
            <label class="block text-sm">
              描述
              <Textarea v-model="categoryForm.description" class="mt-1" />
            </label>
            <div class="grid grid-cols-2 gap-3">
              <label class="block text-sm">
                排序
                <Input v-model.number="categoryForm.sortOrder" type="number" class="mt-1" />
              </label>
              <label class="block text-sm">
                状态
                <Select
                  :model-value="categoryForm.status"
                  class="mt-1"
                  @update:model-value="categoryForm.status = Number($event)"
                >
                  <option :value="1">显示</option>
                  <option :value="0">隐藏</option>
                </Select>
              </label>
            </div>
            <div class="flex gap-2">
              <Button type="submit" :disabled="loading">保存分类</Button>
              <Button type="button" variant="outline" @click="resetCategoryForm">重置</Button>
            </div>
          </form>

          <form v-else class="flex flex-col gap-3" @submit.prevent="submitTag">
            <label class="block text-sm">
              标签名称
              <Input
                v-model="tagForm.name"
                class="mt-1"
                @input="tagForm.slug = tagForm.slug || toSlug(tagForm.name)"
              />
            </label>
            <label class="block text-sm">
              访问标识
              <Input v-model="tagForm.slug" class="mt-1" @input="tagForm.slug = toSlug(tagForm.slug)" />
            </label>
            <label class="block text-sm">
              颜色
              <Input v-model="tagForm.color" class="mt-1" />
            </label>
            <div class="flex gap-2">
              <Button type="submit" :disabled="loading">保存标签</Button>
              <Button type="button" variant="outline" @click="resetTagForm">重置</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
