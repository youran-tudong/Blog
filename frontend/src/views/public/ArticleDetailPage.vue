<script setup lang="ts">
import { ArrowLeft, ArrowUp, Heart, Link, ListOrdered, LoaderCircle, RefreshCw, Reply, Share2, X } from 'lucide-vue-next'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import type { ArticleItem, ArticleListItem } from '../../api/article'
import {
  listPublicCommentsApi,
  submitCommentApi,
  type CommentPayload,
  type PublicCommentItem,
} from '../../api/comment'
import { createCaptchaApi, type CaptchaItem } from '../../api/guard'
import {
  getPublicArticleApi,
  getPublicArticleLikeStatusApi,
  likePublicArticleApi,
  listRelatedPublicArticlesApi,
  unlikePublicArticleApi,
} from '../../api/publicArticle'
import { Alert } from '../../components/ui/alert'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { Textarea } from '../../components/ui/textarea'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import { writeTextToClipboard } from '../../lib/clipboard'
import { setDocumentHead } from '../../lib/head'
import { getOrCreateVisitorKey } from '../../lib/visitor'
import {
  copyMarkdownCodeFromEvent,
  extractMarkdownHeadings,
  renderMarkdown,
  type MarkdownHeading,
} from '../../lib/markdown'

const route = useRoute()
const router = useRouter()
const detailTopRef = ref<HTMLElement>()
const commentFormRef = ref<HTMLFormElement>()
const article = ref<ArticleItem>()
const comments = ref<PublicCommentItem[]>([])
const relatedArticles = ref<ArticleListItem[]>([])
const replyTarget = ref<PublicCommentItem>()
const captcha = ref<CaptchaItem>()
const captchaAnswer = ref('')
const captchaLoading = ref(false)
const captchaError = ref('')
const loading = ref(true)
const commentsLoading = ref(false)
const relatedLoading = ref(false)
const likeStatusLoading = ref(false)
const likeSubmitting = ref(false)
const liked = ref(false)
const likeCount = ref(0)
const commentSubmitting = ref(false)
const error = ref('')
const commentsError = ref('')
const relatedError = ref('')
const likeError = ref('')
const likeMessage = ref('')
const shareMessage = ref('')
const shareError = ref('')
const copySubmitting = ref(false)
const shareSubmitting = ref(false)
const commentMessage = ref('')
const commentError = ref('')
let articleLoadRequestId = 0
const slug = computed(() => String(route.params.slug || ''))
const articleHtml = computed(() => renderMarkdown(article.value?.content))
const articleHeadings = computed(() => extractMarkdownHeadings(article.value?.content))
const articleShareUrl = computed(() => {
  if (typeof window === 'undefined') {
    return article.value ? `/articles/${article.value.slug}` : ''
  }
  return new URL(article.value ? `/articles/${article.value.slug}` : route.fullPath, window.location.origin).toString()
})
const articleShellClass = computed(() =>
  articleHeadings.value.length > 0 ? 'grid gap-3 xl:grid-cols-[minmax(0,1fr)_220px]' : 'flex flex-col gap-3',
)
const commentForm = reactive<Omit<CommentPayload, 'articleId'>>({
  parentId: 0,
  nickname: '',
  email: '',
  website: '',
  content: '',
})
const visitorKey = getOrCreateVisitorKey()

const loadComments = async (articleId: number, requestId: number) => {
  commentsLoading.value = true
  commentsError.value = ''
  try {
    const loadedComments = await listPublicCommentsApi(articleId)
    if (requestId !== articleLoadRequestId) return
    comments.value = loadedComments
  } catch (err) {
    if (requestId !== articleLoadRequestId) return
    commentsError.value = err instanceof Error ? err.message : '评论加载失败'
    comments.value = []
  } finally {
    if (requestId === articleLoadRequestId) {
      commentsLoading.value = false
    }
  }
}

const loadRelatedArticles = async (articleSlug: string, requestId: number) => {
  relatedLoading.value = true
  relatedError.value = ''
  try {
    const loadedArticles = await listRelatedPublicArticlesApi(articleSlug, 4)
    if (requestId !== articleLoadRequestId) return
    relatedArticles.value = loadedArticles
  } catch (err) {
    if (requestId !== articleLoadRequestId) return
    relatedError.value = err instanceof Error ? err.message : '相关文章加载失败'
    relatedArticles.value = []
  } finally {
    if (requestId === articleLoadRequestId) {
      relatedLoading.value = false
    }
  }
}

const loadLikeStatus = async (articleSlug: string, requestId: number) => {
  likeStatusLoading.value = true
  likeError.value = ''
  try {
    const status = await getPublicArticleLikeStatusApi(articleSlug, visitorKey)
    if (requestId !== articleLoadRequestId) return
    liked.value = status.liked
    likeCount.value = status.likeCount
  } catch (err) {
    if (requestId !== articleLoadRequestId) return
    likeError.value = err instanceof Error ? err.message : '点赞状态加载失败'
  } finally {
    if (requestId === articleLoadRequestId) {
      likeStatusLoading.value = false
    }
  }
}

const loadArticle = async () => {
  const requestId = ++articleLoadRequestId
  setDocumentHead({
    title: '文章详情',
    description: '正在加载文章内容。',
    canonicalPath: route.fullPath,
    type: 'article',
    structuredData: null,
  })
  loading.value = true
  error.value = ''
  article.value = undefined
  comments.value = []
  relatedArticles.value = []
  replyTarget.value = undefined
  commentForm.parentId = 0
  commentsLoading.value = false
  relatedLoading.value = false
  likeStatusLoading.value = false
  likeSubmitting.value = false
  liked.value = false
  likeCount.value = 0
  commentsError.value = ''
  relatedError.value = ''
  likeError.value = ''
  likeMessage.value = ''
  shareMessage.value = ''
  shareError.value = ''
  copySubmitting.value = false
  shareSubmitting.value = false
  try {
    const loadedArticle = await getPublicArticleApi(slug.value)
    if (requestId !== articleLoadRequestId) return
    article.value = loadedArticle
    setDocumentHead({
      title: loadedArticle.title,
      description: loadedArticle.summary || '阅读 TechNote 技术文章。',
      keywords: loadedArticle.tagNames?.join(','),
      canonicalPath: `/articles/${loadedArticle.slug}`,
      type: 'article',
      structuredData: {
        '@context': 'https://schema.org',
        '@type': 'BlogPosting',
        headline: loadedArticle.title,
        description: loadedArticle.summary || loadedArticle.title,
        url: articleShareUrl.value,
        datePublished: loadedArticle.publishTime || loadedArticle.createTime,
        dateModified: loadedArticle.updateTime || loadedArticle.publishTime || loadedArticle.createTime,
        author: {
          '@type': 'Person',
          name: 'TechNote Admin',
        },
        keywords: loadedArticle.tagNames?.join(','),
      },
    })
    likeCount.value = loadedArticle.likeCount || 0
    loading.value = false
    void loadComments(loadedArticle.id, requestId)
    void loadRelatedArticles(loadedArticle.slug, requestId)
    void loadLikeStatus(loadedArticle.slug, requestId)
  } catch (err) {
    if (requestId !== articleLoadRequestId) return
    error.value = err instanceof Error ? err.message : '文章加载失败'
    setDocumentHead({
      title: '文章不可访问',
      description: '当前文章不存在或暂时无法访问。',
      canonicalPath: route.fullPath,
      type: 'article',
      structuredData: null,
    })
  } finally {
    if (requestId === articleLoadRequestId) {
      loading.value = false
    }
  }
}

const loadCaptcha = async () => {
  captchaLoading.value = true
  captchaError.value = ''
  try {
    captcha.value = await createCaptchaApi()
  } catch (err) {
    captcha.value = undefined
    captchaError.value = err instanceof Error ? err.message : '验证码加载失败'
  } finally {
    captchaLoading.value = false
  }
}

onMounted(() => {
  loadArticle()
  loadCaptcha()
})
watch(slug, loadArticle)

const submitComment = async () => {
  if (!article.value || commentSubmitting.value) return
  commentMessage.value = ''
  commentError.value = ''
  const nickname = commentForm.nickname.trim()
  const content = commentForm.content.trim()
  if (!nickname || !content) {
    commentError.value = '请填写昵称和评论内容'
    return
  }
  const captchaId = captcha.value?.captchaId
  const captchaAnswerValue = captchaAnswer.value.trim()
  const captchaRequired = captcha.value?.expireSeconds !== 0
  if (captchaRequired && (!captchaId || !captchaAnswerValue)) {
    commentError.value = '请先完成验证码'
    return
  }
  commentSubmitting.value = true
  try {
    await submitCommentApi({
      articleId: article.value.id,
      ...commentForm,
      nickname,
      email: commentForm.email?.trim() || undefined,
      website: commentForm.website?.trim() || undefined,
      content,
      captchaId: captchaId || undefined,
      captchaAnswer: captchaAnswerValue || undefined,
    })
    commentForm.nickname = ''
    commentForm.email = ''
    commentForm.website = ''
    commentForm.content = ''
    captchaAnswer.value = ''
    commentForm.parentId = 0
    replyTarget.value = undefined
    commentMessage.value = '评论已提交，审核通过后会展示'
  } catch (err) {
    commentError.value = err instanceof Error ? err.message : '评论提交失败'
  } finally {
    commentSubmitting.value = false
    await loadCaptcha()
  }
}

const startReply = (comment: PublicCommentItem) => {
  replyTarget.value = comment
  commentForm.parentId = comment.id
  commentMessage.value = ''
  commentError.value = ''
  void nextTick(() => commentFormRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' }))
}

const cancelReply = () => {
  replyTarget.value = undefined
  commentForm.parentId = 0
}

const toggleLike = async () => {
  if (!article.value || likeSubmitting.value || likeStatusLoading.value) return
  const articleSlug = article.value.slug
  likeSubmitting.value = true
  likeMessage.value = ''
  likeError.value = ''
  try {
    const status = liked.value
      ? await unlikePublicArticleApi(articleSlug, visitorKey)
      : await likePublicArticleApi(articleSlug, visitorKey)
    if (article.value?.slug !== articleSlug) return
    liked.value = status.liked
    likeCount.value = status.likeCount
    article.value.likeCount = status.likeCount
    likeMessage.value = status.liked ? '感谢你的认可' : '已取消点赞'
  } catch (err) {
    if (article.value?.slug !== articleSlug) return
    likeError.value = err instanceof Error ? err.message : '点赞操作失败'
  } finally {
    if (article.value?.slug === articleSlug) {
      likeSubmitting.value = false
    }
  }
}

const copyArticleLink = async () => {
  if (!article.value || copySubmitting.value || shareSubmitting.value) return
  copySubmitting.value = true
  shareMessage.value = ''
  shareError.value = ''
  try {
    await writeTextToClipboard(articleShareUrl.value)
    shareMessage.value = '文章链接已复制'
  } catch (err) {
    shareError.value = err instanceof Error ? err.message : '复制链接失败'
  } finally {
    copySubmitting.value = false
  }
}

const shareArticle = async () => {
  if (!article.value || copySubmitting.value || shareSubmitting.value) return
  shareSubmitting.value = true
  shareMessage.value = ''
  shareError.value = ''
  try {
    if (navigator.share) {
      await navigator.share({
        title: article.value.title,
        text: article.value.summary || article.value.title,
        url: articleShareUrl.value,
      })
      shareMessage.value = '分享面板已打开'
      return
    }
    await writeTextToClipboard(articleShareUrl.value)
    shareMessage.value = '当前浏览器不支持系统分享，已复制链接'
  } catch (err) {
    if (err instanceof DOMException && err.name === 'AbortError') {
      shareMessage.value = ''
      return
    }
    shareError.value = err instanceof Error ? err.message : '分享失败'
  } finally {
    shareSubmitting.value = false
  }
}

const handleMarkdownClick = (event: MouseEvent) => {
  copyMarkdownCodeFromEvent(event)
}

const headingClass = (heading: MarkdownHeading) => {
  const indentMap: Record<number, string> = {
    1: '',
    2: 'pl-3',
    3: 'pl-6',
    4: 'pl-9',
  }
  return `block truncate rounded-md px-2 py-1.5 text-sm text-muted-foreground hover:bg-hover hover:text-foreground ${
    indentMap[heading.level] || ''
  }`
}

const scrollToTop = () => {
  detailTopRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<template>
  <div ref="detailTopRef" class="flex flex-col gap-3">
    <Button variant="ghost" class="w-fit" @click="router.push('/')">
      <ArrowLeft data-icon="inline-start" />
      返回首页
    </Button>

    <Alert v-if="error" variant="destructive">{{ error }}</Alert>

    <LoadingState
      v-if="loading"
      title="正在加载文章"
      description="正在读取正文和文章目录。"
    />

    <div v-else-if="article" :class="articleShellClass">
      <Card class="min-w-0">
        <CardHeader class="gap-3">
          <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <Badge v-if="article.topFlag === 1">置顶</Badge>
            <span>{{ article.categoryName || '未分类' }}</span>
            <RouterLink
              v-if="article.columnName"
              :to="article.columnSlug ? `/columns/${article.columnSlug}` : { path: '/', query: { columnId: article.columnId } }"
              class="hover:text-foreground"
            >
              专栏：{{ article.columnName }}
            </RouterLink>
            <span>{{ article.publishTime || article.createTime }}</span>
            <span>{{ article.viewCount }} 阅读</span>
          </div>
          <CardTitle class="break-words text-2xl leading-tight sm:text-3xl">{{ article.title }}</CardTitle>
          <p v-if="article.summary" class="break-words text-sm leading-6 text-muted-foreground">{{ article.summary }}</p>
          <div class="flex flex-wrap gap-2">
            <Badge v-for="tag in article.tagNames" :key="tag" variant="secondary">{{ tag }}</Badge>
          </div>
        </CardHeader>
        <CardContent>
          <article
            class="flex flex-col gap-3 text-sm leading-7 text-foreground [overflow-wrap:anywhere] [&_a]:text-primary [&_blockquote]:rounded-md [&_blockquote]:border-l-4 [&_blockquote]:bg-surface [&_blockquote]:p-3 [&_code]:rounded-sm [&_code]:bg-surface [&_code]:px-1 [&_h1]:scroll-mt-32 [&_h2]:scroll-mt-32 [&_h3]:scroll-mt-32 [&_h4]:scroll-mt-32 [&_img]:max-h-[520px] [&_img]:w-full [&_img]:rounded-md [&_img]:border [&_img]:object-contain [&_pre]:max-w-full [&_pre]:overflow-auto [&_pre]:rounded-md [&_pre]:border [&_pre]:bg-surface [&_pre]:p-3 [&_ul]:list-disc [&_ul]:pl-5 lg:[&_h1]:scroll-mt-20 lg:[&_h2]:scroll-mt-20 lg:[&_h3]:scroll-mt-20 lg:[&_h4]:scroll-mt-20"
            @click="handleMarkdownClick"
            v-html="articleHtml"
          />
          <div class="mt-6 flex flex-wrap items-center gap-3 border-t pt-4">
            <Button
              variant="outline"
              :disabled="likeSubmitting || likeStatusLoading"
              :aria-pressed="liked"
              @click="toggleLike"
            >
              <LoaderCircle v-if="likeSubmitting || likeStatusLoading" class="animate-spin" data-icon="inline-start" />
              <Heart
                v-else
                :class="liked ? 'text-destructive' : ''"
                :fill="liked ? 'currentColor' : 'none'"
                data-icon="inline-start"
              />
              {{ liked ? '已点赞' : '点赞' }} {{ likeCount }}
            </Button>
            <Button variant="outline" :disabled="copySubmitting || shareSubmitting" @click="copyArticleLink">
              <LoaderCircle v-if="copySubmitting" class="animate-spin" data-icon="inline-start" />
              <Link v-else data-icon="inline-start" />
              复制链接
            </Button>
            <Button variant="outline" :disabled="copySubmitting || shareSubmitting" @click="shareArticle">
              <LoaderCircle v-if="shareSubmitting" class="animate-spin" data-icon="inline-start" />
              <Share2 v-else data-icon="inline-start" />
              分享
            </Button>
            <span v-if="likeMessage" class="text-sm text-muted-foreground">{{ likeMessage }}</span>
            <span v-else-if="likeError" class="text-sm text-destructive">{{ likeError }}</span>
            <span v-if="shareMessage" class="text-sm text-muted-foreground">{{ shareMessage }}</span>
            <span v-else-if="shareError" class="text-sm text-destructive">{{ shareError }}</span>
          </div>
        </CardContent>
      </Card>

      <Card v-if="articleHeadings.length > 0" class="order-first h-fit xl:order-none xl:sticky xl:top-20">
        <CardHeader class="pb-2">
          <CardTitle class="flex items-center gap-2 text-base">
            <ListOrdered :size="16" />
            文章目录
          </CardTitle>
        </CardHeader>
        <CardContent class="flex max-h-[420px] flex-col gap-1 overflow-auto">
          <a v-for="heading in articleHeadings" :key="heading.id" :href="`#${heading.id}`" :class="headingClass(heading)">
            {{ heading.text }}
          </a>
        </CardContent>
      </Card>
    </div>

    <Card v-if="article">
      <CardHeader>
        <CardTitle class="text-lg">相关文章</CardTitle>
      </CardHeader>
      <CardContent>
        <LoadingState v-if="relatedLoading" title="正在查找相关文章" />
        <Alert v-else-if="relatedError" variant="destructive">{{ relatedError }}</Alert>
        <div v-else-if="relatedArticles.length > 0" class="grid gap-3 sm:grid-cols-2">
          <RouterLink
            v-for="item in relatedArticles"
            :key="item.id"
            :to="`/articles/${item.slug}`"
            class="flex min-w-0 flex-col gap-2 rounded-md border p-3 transition-colors hover:bg-hover"
          >
            <div class="line-clamp-2 break-words text-sm font-medium leading-6">{{ item.title }}</div>
            <p class="line-clamp-2 break-words text-xs leading-5 text-muted-foreground">
              {{ item.summary || '继续阅读这篇相关技术文章。' }}
            </p>
            <div class="mt-auto flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
              <span>{{ item.categoryName || '未分类' }}</span>
              <span>{{ item.viewCount || 0 }} 阅读</span>
            </div>
          </RouterLink>
        </div>
        <div v-else class="rounded-md border p-6 text-center text-sm text-muted-foreground">
          暂无可推荐的相关文章
        </div>
      </CardContent>
    </Card>

    <Card v-if="article">
      <CardHeader>
        <CardTitle class="text-lg">评论</CardTitle>
      </CardHeader>
      <CardContent class="flex flex-col gap-4">
        <LoadingState v-if="commentsLoading" title="正在加载评论" />
        <Alert v-else-if="commentsError" variant="destructive">{{ commentsError }}</Alert>
        <div v-else-if="comments.length > 0" class="flex flex-col gap-3">
          <div v-for="item in comments" :key="item.id" class="rounded-md border p-3">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="flex flex-wrap items-center gap-2 text-sm">
                <span class="font-medium">{{ item.nickname }}</span>
                <span class="text-xs text-muted-foreground">{{ item.createTime }}</span>
              </div>
              <Button variant="ghost" size="sm" @click="startReply(item)">
                <Reply data-icon="inline-start" />
                回复
              </Button>
            </div>
            <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-6">{{ item.content }}</p>
            <div v-if="item.replyContent" class="mt-3 break-words rounded-md bg-surface p-3 text-sm leading-6 text-muted-foreground">
              回复：{{ item.replyContent }}
            </div>
            <div v-if="item.replies.length > 0" class="mt-3 flex flex-col gap-3 border-l pl-3">
              <div v-for="replyItem in item.replies" :key="replyItem.id" class="rounded-md bg-surface p-3">
                <div class="flex flex-wrap items-center gap-2 text-sm">
                  <span class="font-medium">{{ replyItem.nickname }}</span>
                  <span class="text-xs text-muted-foreground">{{ replyItem.createTime }}</span>
                </div>
                <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-6">{{ replyItem.content }}</p>
                <div
                  v-if="replyItem.replyContent"
                  class="mt-3 break-words rounded-md border bg-background p-3 text-sm leading-6 text-muted-foreground"
                >
                  回复：{{ replyItem.replyContent }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="rounded-md border p-6 text-center text-sm text-muted-foreground">暂无已通过评论</div>

        <Alert v-if="commentMessage || commentError" :variant="commentError ? 'destructive' : 'success'">
          {{ commentError || commentMessage }}
        </Alert>

        <form
          ref="commentFormRef"
          class="grid gap-3 sm:grid-cols-2"
          :aria-busy="commentSubmitting"
          @submit.prevent="submitComment"
        >
          <div
            v-if="replyTarget"
            class="flex flex-wrap items-center justify-between gap-2 rounded-md border bg-surface p-3 text-sm sm:col-span-2"
          >
            <span class="break-words">正在回复 {{ replyTarget.nickname }}</span>
            <Button type="button" variant="ghost" size="sm" :disabled="commentSubmitting" @click="cancelReply">
              <X data-icon="inline-start" />
              取消回复
            </Button>
          </div>
          <Input v-model="commentForm.nickname" :disabled="commentSubmitting" placeholder="昵称" />
          <Input v-model="commentForm.email" :disabled="commentSubmitting" placeholder="邮箱，可选" />
          <Input v-model="commentForm.website" class="sm:col-span-2" :disabled="commentSubmitting" placeholder="个人网站，可选" />
          <Textarea
            v-model="commentForm.content"
            class="sm:col-span-2"
            :disabled="commentSubmitting"
            :placeholder="replyTarget ? `回复 ${replyTarget.nickname}，提交后等待审核` : '写下评论，提交后等待审核'"
          />
          <div class="flex flex-col gap-2 sm:col-span-2">
            <div class="flex flex-wrap items-center gap-2 text-sm">
              <span class="text-muted-foreground">验证码</span>
              <span class="rounded-md border bg-surface px-3 py-1 font-medium">
                {{ captchaLoading ? '加载中' : captcha?.question || '加载失败' }}
              </span>
              <Button type="button" variant="ghost" size="sm" :disabled="commentSubmitting || captchaLoading" @click="loadCaptcha">
                <RefreshCw data-icon="inline-start" />
                换一题
              </Button>
            </div>
            <Input
              v-model="captchaAnswer"
              inputmode="numeric"
              autocomplete="off"
              :disabled="commentSubmitting || captchaLoading"
              placeholder="输入计算结果"
            />
            <span v-if="captchaError" class="text-sm text-destructive">{{ captchaError }}</span>
          </div>
          <Button class="min-w-24 w-fit" type="submit" :disabled="commentSubmitting">
            <LoaderCircle v-if="commentSubmitting" class="animate-spin" data-icon="inline-start" />
            {{ commentSubmitting ? '提交中' : '提交评论' }}
          </Button>
        </form>
      </CardContent>
    </Card>

    <EmptyState
      v-else-if="!error && !loading"
      title="暂无文章内容"
      description="当前文章不存在或暂时无法访问，可以返回首页继续浏览。"
      action-label="返回首页"
      action-to="/"
    />

    <Button
      v-if="article"
      variant="outline"
      size="icon"
      class="fixed bottom-4 right-3 z-30 shadow-sm sm:right-4"
      aria-label="回到顶部"
      @click="scrollToTop"
    >
      <ArrowUp />
    </Button>
  </div>
</template>
