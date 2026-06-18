<script setup lang="ts">
import { History, ImagePlus } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createArticleApi,
  deleteArticleAutoDraftApi,
  getArticleAutoDraftApi,
  getArticleApi,
  getArticleVersionApi,
  listArticleVersionsApi,
  rollbackArticleVersionApi,
  saveArticleAutoDraftApi,
  updateArticleApi,
  type ArticleAutoDraftItem,
  type ArticleAutoDraftPayload,
  type ArticleItem,
  type ArticlePayload,
  type ArticleVersionDetail,
  type ArticleVersionItem,
} from '../../api/article'
import { pageColumnsApi, type ColumnItem } from '../../api/column'
import { uploadImageApi } from '../../api/media'
import { pageCategoriesApi, pageTagsApi, type CategoryItem, type TagItem } from '../../api/taxonomy'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Checkbox } from '../../components/ui/checkbox'
import { Input } from '../../components/ui/input'
import { Select } from '../../components/ui/select'
import { Textarea } from '../../components/ui/textarea'
import { copyMarkdownCodeFromEvent, renderMarkdown } from '../../lib/markdown'

const route = useRoute()
const router = useRouter()
const articleId = computed(() => (route.params.id ? Number(route.params.id) : undefined))
const categories = ref<CategoryItem[]>([])
const columns = ref<ColumnItem[]>([])
const tags = ref<TagItem[]>([])
const loading = ref(false)
const uploading = ref(false)
const error = ref('')
const message = ref('')
const coverInputRef = ref<HTMLInputElement>()
const contentImageInputRef = ref<HTMLInputElement>()
const contentMode = ref<'edit' | 'preview'>('edit')
const versions = ref<ArticleVersionItem[]>([])
const selectedVersion = ref<ArticleVersionDetail>()
const versionLoading = ref(false)
const draftKey = ref('')
const autoSaveState = ref<'idle' | 'saving' | 'saved' | 'error'>('idle')
const autoSaveError = ref('')
const lastAutoSavedAt = ref('')
const lastDraftSnapshot = ref('')
let autoSaveTimer: number | undefined

const AUTO_SAVE_INTERVAL_MS = 30000
const NEW_DRAFT_KEY_STORAGE = 'technote-new-article-draft-key'

const form = reactive<ArticlePayload>({
  title: '',
  slug: '',
  summary: '',
  content: '',
  coverUrl: '',
  categoryId: undefined,
  columnId: undefined,
  tagIds: [],
  topFlag: 0,
  visibility: 1,
  status: 0,
})

const previewHtml = computed(() => renderMarkdown(form.content))
const selectedVersionHtml = computed(() => renderMarkdown(selectedVersion.value?.content))
const autoSaveStatusText = computed(() => {
  if (autoSaveState.value === 'saving') {
    return '自动保存中...'
  }
  if (autoSaveState.value === 'saved') {
    return `已自动保存 ${lastAutoSavedAt.value || ''}`
  }
  if (autoSaveState.value === 'error') {
    return autoSaveError.value ? `自动保存失败：${autoSaveError.value}` : '自动保存失败'
  }
  return '自动保存已开启，每 30 秒保存一次'
})

const toSlug = (value: string) =>
  value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')

const updateTitle = (value: string | number) => {
  form.title = String(value)
  if (!form.slug) {
    form.slug = toSlug(form.title)
  }
}

const updateSlug = (value: string | number) => {
  form.slug = toSlug(String(value))
}

const formatDraftTime = (value?: string) => {
  if (!value) {
    return ''
  }
  return value.replace('T', ' ').slice(0, 19)
}

const createNewDraftKey = () => `new-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`

const resolveDraftKey = () => {
  if (articleId.value) {
    return `article-${articleId.value}`
  }
  const cached = localStorage.getItem(NEW_DRAFT_KEY_STORAGE)
  if (cached) {
    return cached
  }
  const nextKey = createNewDraftKey()
  localStorage.setItem(NEW_DRAFT_KEY_STORAGE, nextKey)
  return nextKey
}

const buildAutoDraftPayload = (override?: { draftKey?: string; articleId?: number }): ArticleAutoDraftPayload => ({
  draftKey: override?.draftKey ?? draftKey.value,
  articleId: override?.articleId ?? articleId.value,
  title: form.title,
  slug: form.slug ? toSlug(form.slug) : '',
  summary: form.summary,
  content: form.content,
  coverUrl: form.coverUrl,
  categoryId: form.categoryId,
  columnId: form.columnId,
  tagIds: [...form.tagIds],
  topFlag: form.topFlag,
  visibility: form.visibility,
})

const createDraftSnapshot = (payload: ArticleAutoDraftPayload) =>
  JSON.stringify({
    articleId: payload.articleId,
    title: payload.title || '',
    slug: payload.slug || '',
    summary: payload.summary || '',
    content: payload.content || '',
    coverUrl: payload.coverUrl || '',
    categoryId: payload.categoryId,
    columnId: payload.columnId,
    tagIds: [...(payload.tagIds || [])].sort((a, b) => a - b),
    topFlag: payload.topFlag ?? 0,
    visibility: payload.visibility ?? 1,
  })

const createDraftSnapshotFromDraft = (draft: ArticleAutoDraftItem) =>
  createDraftSnapshot({
    draftKey: draft.draftKey,
    articleId: draft.articleId ?? undefined,
    title: draft.title || '',
    slug: draft.slug || '',
    summary: draft.summary || '',
    content: draft.content || '',
    coverUrl: draft.coverUrl || '',
    categoryId: draft.categoryId ?? undefined,
    columnId: draft.columnId ?? undefined,
    tagIds: draft.tagIds || [],
    topFlag: draft.topFlag ?? 0,
    visibility: draft.visibility ?? 1,
  })

const hasDraftContent = (payload: ArticleAutoDraftPayload) =>
  Boolean(
    payload.title?.trim() ||
      payload.slug?.trim() ||
      payload.summary?.trim() ||
      payload.content?.trim() ||
      payload.coverUrl?.trim() ||
      payload.categoryId ||
      payload.columnId ||
      payload.tagIds.length,
  )

const isTagChecked = (tagId: number) => form.tagIds.includes(tagId)

const toggleTag = (tagId: number, checked: boolean) => {
  if (checked && !form.tagIds.includes(tagId)) {
    form.tagIds.push(tagId)
  }
  if (!checked) {
    form.tagIds = form.tagIds.filter((id) => id !== tagId)
  }
}

const applyArticleToForm = (article: ArticleItem) => {
  form.title = article.title
  form.slug = article.slug
  form.summary = article.summary || ''
  form.content = article.content
  form.coverUrl = article.coverUrl || ''
  form.categoryId = article.categoryId
  form.columnId = article.columnId
  form.tagIds = article.tagIds || []
  form.topFlag = article.topFlag
  form.visibility = article.visibility
  form.status = article.status
}

const applyAutoDraftToForm = (draft: ArticleAutoDraftItem) => {
  form.title = draft.title || ''
  form.slug = draft.slug || ''
  form.summary = draft.summary || ''
  form.content = draft.content || ''
  form.coverUrl = draft.coverUrl || ''
  form.categoryId = draft.categoryId ?? undefined
  form.columnId = draft.columnId ?? undefined
  form.tagIds = draft.tagIds || []
  form.topFlag = draft.topFlag ?? 0
  form.visibility = draft.visibility ?? 1
}

const loadOptions = async () => {
  const [categoryPage, columnPage, tagPage] = await Promise.all([
    pageCategoriesApi({ pageNo: 1, pageSize: 100 }),
    pageColumnsApi({ pageNo: 1, pageSize: 100 }),
    pageTagsApi({ pageNo: 1, pageSize: 100 }),
  ])
  categories.value = categoryPage.records
  columns.value = columnPage.records
  tags.value = tagPage.records
}

const loadArticle = async () => {
  if (!articleId.value) {
    return
  }
  const article = await getArticleApi(articleId.value)
  applyArticleToForm(article)
}

const loadVersions = async () => {
  if (!articleId.value) {
    versions.value = []
    return
  }
  versions.value = await listArticleVersionsApi(articleId.value)
}

const loadAutoDraft = async () => {
  if (!draftKey.value) {
    return
  }
  try {
    const draft = await getArticleAutoDraftApi(draftKey.value)
    if (!draft) {
      return
    }
    const draftSnapshot = createDraftSnapshotFromDraft(draft)
    lastAutoSavedAt.value = formatDraftTime(draft.updateTime)
    if (draftSnapshot === createDraftSnapshot(buildAutoDraftPayload())) {
      lastDraftSnapshot.value = draftSnapshot
      return
    }
    const timeText = lastAutoSavedAt.value || '未知时间'
    if (window.confirm(`检测到 ${timeText} 的自动草稿，是否恢复？`)) {
      applyAutoDraftToForm(draft)
      lastDraftSnapshot.value = createDraftSnapshot(buildAutoDraftPayload())
      message.value = '已恢复自动草稿'
    }
  } catch (err) {
    autoSaveState.value = 'error'
    autoSaveError.value = err instanceof Error ? err.message : '自动草稿加载失败'
  }
}

const autoSaveArticle = async () => {
  if (!draftKey.value || loading.value || uploading.value || autoSaveState.value === 'saving') {
    return
  }
  const payload = buildAutoDraftPayload()
  if (!hasDraftContent(payload)) {
    return
  }
  const snapshot = createDraftSnapshot(payload)
  if (snapshot === lastDraftSnapshot.value) {
    return
  }
  autoSaveState.value = 'saving'
  autoSaveError.value = ''
  try {
    const draft = await saveArticleAutoDraftApi(payload)
    lastAutoSavedAt.value = formatDraftTime(draft.updateTime)
    lastDraftSnapshot.value = snapshot
    autoSaveState.value = 'saved'
  } catch (err) {
    autoSaveState.value = 'error'
    autoSaveError.value = err instanceof Error ? err.message : '自动保存失败'
  }
}

const startAutoSave = () => {
  autoSaveTimer = window.setInterval(autoSaveArticle, AUTO_SAVE_INTERVAL_MS)
}

const cleanupAutoDraft = async (key = draftKey.value) => {
  if (!key) {
    return
  }
  try {
    await deleteArticleAutoDraftApi(key)
  } catch (err) {
    autoSaveState.value = 'error'
    autoSaveError.value = err instanceof Error ? err.message : '自动草稿清理失败'
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await loadOptions()
    await loadArticle()
    draftKey.value = resolveDraftKey()
    await loadAutoDraft()
    await loadVersions()
    startAutoSave()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '页面加载失败'
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  if (autoSaveTimer) {
    window.clearInterval(autoSaveTimer)
  }
})

watch(
  form,
  () => {
    if (autoSaveState.value === 'saved') {
      autoSaveState.value = 'idle'
    }
  },
  { deep: true },
)

const saveArticle = async () => {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const payload: ArticlePayload = { ...form, slug: toSlug(form.slug) }
    const currentDraftKey = draftKey.value
    if (articleId.value) {
      await updateArticleApi(articleId.value, payload)
      lastDraftSnapshot.value = createDraftSnapshot(buildAutoDraftPayload())
      await cleanupAutoDraft(currentDraftKey)
      message.value = '文章已更新，已生成编辑前历史版本'
      await loadVersions()
    } else {
      const created = await createArticleApi(payload)
      await cleanupAutoDraft(currentDraftKey)
      localStorage.removeItem(NEW_DRAFT_KEY_STORAGE)
      draftKey.value = `article-${created.id}`
      lastDraftSnapshot.value = createDraftSnapshot(buildAutoDraftPayload({ draftKey: draftKey.value, articleId: created.id }))
      message.value = '文章已创建'
      await router.replace(`/admin/articles/${created.id}/edit`)
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    loading.value = false
  }
}

const viewVersion = async (versionId: number) => {
  if (!articleId.value) {
    return
  }
  versionLoading.value = true
  error.value = ''
  try {
    selectedVersion.value = await getArticleVersionApi(articleId.value, versionId)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '历史版本加载失败'
  } finally {
    versionLoading.value = false
  }
}

const rollbackVersion = async (versionId: number) => {
  if (!articleId.value) {
    return
  }
  if (!window.confirm('确认回滚到该历史版本吗？当前内容会先保存为新的历史版本。')) {
    return
  }
  versionLoading.value = true
  error.value = ''
  message.value = ''
  try {
    const article = await rollbackArticleVersionApi(articleId.value, versionId)
    applyArticleToForm(article)
    selectedVersion.value = undefined
    await loadVersions()
    message.value = '文章已回滚，回滚前内容已保存为新的历史版本'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '版本回滚失败'
  } finally {
    versionLoading.value = false
  }
}

const appendMarkdownImage = (alt: string, url: string) => {
  const imageMarkdown = `![${alt}](${url})`
  form.content = form.content ? `${form.content.trimEnd()}\n\n${imageMarkdown}\n` : `${imageMarkdown}\n`
}

const uploadSelectedImage = async (event: Event, usage: 'cover' | 'content') => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  uploading.value = true
  error.value = ''
  message.value = ''
  try {
    const media = await uploadImageApi(file)
    if (usage === 'cover') {
      form.coverUrl = media.filePath
      message.value = '封面图已上传'
    } else {
      appendMarkdownImage(media.originalName, media.filePath)
      message.value = '图片已插入正文'
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '图片上传失败'
  } finally {
    uploading.value = false
    input.value = ''
  }
}
const handleMarkdownClick = (event: MouseEvent) => {
  copyMarkdownCodeFromEvent(event)
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-xl font-semibold">{{ articleId ? '编辑文章' : '新建文章' }}</h1>
        <p class="mt-1 text-sm text-muted-foreground">保存正式修改时，后端会自动生成编辑前历史版本。</p>
      </div>
      <div class="flex flex-col items-stretch gap-2 sm:items-end">
        <div class="flex flex-wrap gap-2">
          <Button variant="outline" @click="router.push('/admin/articles')">返回列表</Button>
          <Button :disabled="loading || uploading" @click="saveArticle">保存文章</Button>
        </div>
        <p class="text-xs text-muted-foreground">{{ autoSaveStatusText }}</p>
      </div>
    </div>

    <Alert v-if="message || error" :variant="error ? 'destructive' : 'success'">
      {{ error || message }}
    </Alert>

    <div class="grid gap-4 xl:grid-cols-[1fr_320px]">
      <Card>
        <CardHeader>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <CardTitle>正文内容</CardTitle>
            <div class="flex rounded-md border bg-surface p-1">
              <Button
                size="sm"
                :variant="contentMode === 'edit' ? 'secondary' : 'ghost'"
                @click="contentMode = 'edit'"
              >
                编辑
              </Button>
              <Button
                size="sm"
                :variant="contentMode === 'preview' ? 'secondary' : 'ghost'"
                @click="contentMode = 'preview'"
              >
                预览
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <label class="block text-sm">
            标题
            <Input :model-value="form.title" class="mt-1" @update:model-value="updateTitle" />
          </label>
          <label class="block text-sm">
            访问标识
            <Input :model-value="form.slug" class="mt-1" @update:model-value="updateSlug" />
          </label>
          <label class="block text-sm">
            摘要
            <Textarea v-model="form.summary" class="mt-1" />
          </label>
          <label v-if="contentMode === 'edit'" class="block text-sm">
            Markdown 正文
            <Textarea v-model="form.content" class="mt-1 min-h-[420px] font-mono" />
          </label>
          <article
            v-else
            class="markdown-preview min-h-[420px] rounded-md border bg-background p-4 text-sm leading-7"
            @click="handleMarkdownClick"
            v-html="previewHtml"
          />
          <div v-if="contentMode === 'edit'">
            <input
              ref="contentImageInputRef"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              class="hidden"
              @change="uploadSelectedImage($event, 'content')"
            />
            <Button variant="outline" :disabled="uploading" @click="contentImageInputRef?.click()">
              <ImagePlus data-icon="inline-start" />
              插入图片
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>发布设置</CardTitle>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <label class="block text-sm">
            分类
            <Select
              :model-value="form.categoryId ?? ''"
              class="mt-1"
              @update:model-value="form.categoryId = $event ? Number($event) : undefined"
            >
              <option value="">不选择分类</option>
              <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
            </Select>
          </label>
          <label class="block text-sm">
            专栏
            <Select
              :model-value="form.columnId ?? ''"
              class="mt-1"
              @update:model-value="form.columnId = $event ? Number($event) : undefined"
            >
              <option value="">不加入专栏</option>
              <option v-for="item in columns" :key="item.id" :value="item.id">{{ item.name }}</option>
            </Select>
          </label>
          <div class="flex flex-col gap-2">
            <div class="text-sm">标签</div>
            <label v-for="item in tags" :key="item.id" class="flex items-center gap-2 text-sm">
              <Checkbox :checked="isTagChecked(item.id)" @update:checked="toggleTag(item.id, $event)" />
              {{ item.name }}
            </label>
            <span v-if="tags.length === 0" class="text-sm text-muted-foreground">暂无标签</span>
          </div>
          <label class="block text-sm">
            封面图
            <Input v-model="form.coverUrl" class="mt-1" />
          </label>
          <div class="flex flex-col gap-2">
            <input
              ref="coverInputRef"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              class="hidden"
              @change="uploadSelectedImage($event, 'cover')"
            />
            <Button variant="outline" :disabled="uploading" @click="coverInputRef?.click()">
              <ImagePlus data-icon="inline-start" />
              上传封面
            </Button>
            <img
              v-if="form.coverUrl"
              :src="form.coverUrl"
              alt="文章封面预览"
              class="aspect-video w-full rounded-md border object-cover"
            />
          </div>
          <label class="block text-sm">
            状态
            <Select
              :model-value="form.status"
              class="mt-1"
              @update:model-value="form.status = Number($event)"
            >
              <option :value="0">草稿</option>
              <option :value="1">发布</option>
              <option :value="2">自动草稿</option>
            </Select>
          </label>
          <label class="block text-sm">
            可见性
            <Select
              :model-value="form.visibility"
              class="mt-1"
              @update:model-value="form.visibility = Number($event)"
            >
              <option :value="1">公开</option>
              <option :value="0">私密</option>
            </Select>
          </label>
          <label class="block text-sm">
            置顶
            <Select
              :model-value="form.topFlag"
              class="mt-1"
              @update:model-value="form.topFlag = Number($event)"
            >
              <option :value="0">不置顶</option>
              <option :value="1">置顶</option>
            </Select>
          </label>
        </CardContent>
      </Card>
    </div>

    <Card v-if="articleId">
      <CardHeader>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <CardTitle class="flex items-center gap-2">
            <History data-icon="inline-start" />
            历史版本
          </CardTitle>
          <Badge variant="outline">{{ versions.length }} 个版本</Badge>
        </div>
      </CardHeader>
      <CardContent class="grid gap-4 xl:grid-cols-[320px_1fr]">
        <div class="flex max-h-[520px] flex-col gap-2 overflow-auto pr-1">
          <div v-for="item in versions" :key="item.id" class="rounded-md border p-3">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <Badge variant="secondary">v{{ item.versionNo }}</Badge>
              <span class="text-xs text-muted-foreground">{{ item.createTime || '-' }}</span>
            </div>
            <div class="mt-2 truncate text-sm font-medium">{{ item.title }}</div>
            <div class="mt-1 line-clamp-2 text-xs text-muted-foreground">
              {{ item.versionRemark || item.summary || '无版本备注' }}
            </div>
            <div class="mt-3 flex flex-wrap gap-2">
              <Button size="sm" variant="outline" :disabled="versionLoading" @click="viewVersion(item.id)">查看</Button>
              <Button size="sm" variant="ghost" :disabled="versionLoading" @click="rollbackVersion(item.id)">回滚</Button>
            </div>
          </div>
          <div v-if="versions.length === 0" class="rounded-md border p-6 text-center text-sm text-muted-foreground">
            暂无历史版本，首次编辑保存后会生成版本快照。
          </div>
        </div>

        <div class="min-w-0 rounded-md border bg-background p-4">
          <template v-if="selectedVersion">
            <div class="flex flex-wrap items-start justify-between gap-3 border-b pb-3">
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <Badge variant="secondary">v{{ selectedVersion.versionNo }}</Badge>
                  <span class="text-xs text-muted-foreground">{{ selectedVersion.createTime || '-' }}</span>
                </div>
                <h2 class="mt-2 truncate text-base font-semibold">{{ selectedVersion.title }}</h2>
                <p v-if="selectedVersion.summary" class="mt-1 line-clamp-2 text-sm text-muted-foreground">
                  {{ selectedVersion.summary }}
                </p>
              </div>
              <Button size="sm" :disabled="versionLoading" @click="rollbackVersion(selectedVersion.id)">
                回滚此版本
              </Button>
            </div>
            <article
              class="markdown-preview mt-4 max-h-[520px] overflow-auto text-sm leading-7"
              @click="handleMarkdownClick"
              v-html="selectedVersionHtml"
            />
          </template>
          <div v-else class="flex min-h-[260px] items-center justify-center text-center text-sm text-muted-foreground">
            选择左侧历史版本后，可在这里查看当时的正文内容。
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<style scoped>
.markdown-preview {
  word-break: break-word;
}

.markdown-preview :deep(a) {
  color: hsl(var(--primary));
}

.markdown-preview :deep(p),
.markdown-preview :deep(blockquote),
.markdown-preview :deep(pre),
.markdown-preview :deep(ul),
.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3),
.markdown-preview :deep(h4) {
  margin: 0 0 0.75rem;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid hsl(var(--border));
  border-radius: 0.375rem;
  background: hsl(var(--surface));
  padding: 0.75rem;
}

.markdown-preview :deep(code) {
  border-radius: 0.25rem;
  background: hsl(var(--surface));
  padding: 0.125rem 0.25rem;
}

.markdown-preview :deep(pre) {
  max-width: 100%;
  overflow: auto;
  border-radius: 0.375rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--surface));
  padding: 0.75rem;
}

.markdown-preview :deep(pre code) {
  padding: 0;
}

.markdown-preview :deep(img) {
  max-height: 520px;
  width: 100%;
  border-radius: 0.375rem;
  border: 1px solid hsl(var(--border));
  object-fit: contain;
}

.markdown-preview :deep(ul) {
  list-style: disc;
  padding-left: 1.25rem;
}
</style>
