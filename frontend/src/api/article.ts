import { request, type ApiResult } from './request'
import type { PageResp } from './taxonomy'

export interface ArticleQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  categoryId?: number
  columnId?: number
  tagId?: number
  status?: number
  visibility?: number
}

export interface ArticleListItem {
  id: number
  title: string
  slug: string
  summary?: string
  coverUrl?: string
  categoryId?: number
  categoryName?: string
  columnId?: number
  columnName?: string
  columnSlug?: string
  tagIds: number[]
  tagNames: string[]
  topFlag: number
  visibility: number
  status: number
  viewCount: number
  likeCount: number
  publishTime?: string
  createTime?: string
  updateTime?: string
}

export interface ArticleItem extends ArticleListItem {
  content: string
}

export interface ArticleVersionItem {
  id: number
  articleId: number
  title: string
  summary?: string
  versionNo: number
  versionRemark?: string
  createBy?: number
  createTime?: string
}

export interface ArticleVersionDetail extends ArticleVersionItem {
  content: string
}

export interface ArticlePayload {
  title: string
  slug: string
  summary?: string
  content: string
  coverUrl?: string
  categoryId?: number
  columnId?: number
  tagIds: number[]
  topFlag: number
  visibility: number
  status: number
}

export interface ArticleAutoDraftPayload {
  draftKey: string
  articleId?: number
  title?: string
  slug?: string
  summary?: string
  content?: string
  coverUrl?: string
  categoryId?: number
  columnId?: number
  tagIds: number[]
  topFlag?: number
  visibility?: number
}

export interface ArticleAutoDraftItem {
  id: number
  draftKey: string
  articleId?: number | null
  title?: string | null
  slug?: string | null
  summary?: string | null
  content?: string | null
  coverUrl?: string | null
  categoryId?: number | null
  columnId?: number | null
  tagIds: number[]
  topFlag: number
  visibility: number
  createTime?: string
  updateTime?: string
}

export interface ArticleAutoDraftQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  articleId?: number
  createBy?: number
  onlyOrphan?: boolean
}

export interface ArticleAutoDraftListItem {
  id: number
  draftKey: string
  articleId?: number | null
  title?: string | null
  slug?: string | null
  summary?: string | null
  contentPreview: string
  contentLength: number
  categoryId?: number | null
  columnId?: number | null
  topFlag: number
  visibility: number
  createBy?: number | null
  createTime?: string
  updateBy?: number | null
  updateTime?: string
}

export interface ArticleAutoDraftCleanupPayload {
  retentionDays: number
  onlyOrphan?: boolean
}

export interface ArticleAutoDraftCleanupResult {
  deletedCount: number
  retentionDays: number
  onlyOrphan: boolean
  cutoffTime?: string
}

export function pageArticlesApi(params: ArticleQuery) {
  return request.get<ApiResult<PageResp<ArticleListItem>>>('/admin/articles', { params }).then((res) => res.data.data)
}

export function getArticleApi(id: number) {
  return request.get<ApiResult<ArticleItem>>(`/admin/articles/${id}`).then((res) => res.data.data)
}

export function createArticleApi(payload: ArticlePayload) {
  return request.post<ApiResult<ArticleItem>>('/admin/articles', payload).then((res) => res.data.data)
}

export function updateArticleApi(id: number, payload: ArticlePayload) {
  return request.put<ApiResult<ArticleItem>>(`/admin/articles/${id}`, payload).then((res) => res.data.data)
}

export function deleteArticleApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/articles/${id}`).then((res) => res.data.data)
}

export function batchDeleteArticlesApi(articleIds: number[]) {
  return request.post<ApiResult<void>>('/admin/articles/batch/delete', { articleIds }).then((res) => res.data.data)
}

export function batchUpdateArticleVisibilityApi(articleIds: number[], visibility: number) {
  return request
    .put<ApiResult<void>>('/admin/articles/batch/visibility', { articleIds, visibility })
    .then((res) => res.data.data)
}

export function batchUpdateArticleCategoryApi(articleIds: number[], categoryId?: number) {
  return request
    .put<ApiResult<void>>('/admin/articles/batch/category', { articleIds, categoryId })
    .then((res) => res.data.data)
}

export function exportArticleMarkdownApi(id: number) {
  return request.get<Blob>(`/admin/articles/${id}/export`, { responseType: 'blob' })
}

export function exportArticlesMarkdownApi(articleIds: number[]) {
  return request.post<Blob>('/admin/articles/export', { articleIds }, { responseType: 'blob' })
}

export function listArticleVersionsApi(id: number) {
  return request.get<ApiResult<ArticleVersionItem[]>>(`/admin/articles/${id}/versions`).then((res) => res.data.data)
}

export function getArticleVersionApi(id: number, versionId: number) {
  return request
    .get<ApiResult<ArticleVersionDetail>>(`/admin/articles/${id}/versions/${versionId}`)
    .then((res) => res.data.data)
}

export function rollbackArticleVersionApi(id: number, versionId: number) {
  return request
    .post<ApiResult<ArticleItem>>(`/admin/articles/${id}/versions/${versionId}/rollback`)
    .then((res) => res.data.data)
}

export function updateArticleTopApi(id: number, value: number) {
  return request.put<ApiResult<void>>(`/admin/articles/${id}/top`, { value }).then((res) => res.data.data)
}

export function updateArticleVisibilityApi(id: number, value: number) {
  return request.put<ApiResult<void>>(`/admin/articles/${id}/visibility`, { value }).then((res) => res.data.data)
}

export function saveArticleAutoDraftApi(payload: ArticleAutoDraftPayload) {
  return request
    .post<ApiResult<ArticleAutoDraftItem>>('/admin/article-auto-drafts', payload)
    .then((res) => res.data.data)
}

export function pageArticleAutoDraftsApi(params: ArticleAutoDraftQuery) {
  return request
    .get<ApiResult<PageResp<ArticleAutoDraftListItem>>>('/admin/article-auto-drafts', { params })
    .then((res) => res.data.data)
}

export function getArticleAutoDraftApi(draftKey: string) {
  return request
    .get<ApiResult<ArticleAutoDraftItem | null>>(`/admin/article-auto-drafts/${encodeURIComponent(draftKey)}`)
    .then((res) => res.data.data)
}

export function getArticleAutoDraftByArticleApi(articleId: number) {
  return request
    .get<ApiResult<ArticleAutoDraftItem | null>>(`/admin/article-auto-drafts/article/${articleId}`)
    .then((res) => res.data.data)
}

export function deleteArticleAutoDraftApi(draftKey: string) {
  return request
    .delete<ApiResult<void>>(`/admin/article-auto-drafts/${encodeURIComponent(draftKey)}`)
    .then((res) => res.data.data)
}

export function deleteArticleAutoDraftByIdApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/article-auto-drafts/id/${id}`).then((res) => res.data.data)
}

export function cleanupArticleAutoDraftsApi(payload: ArticleAutoDraftCleanupPayload) {
  return request
    .post<ApiResult<ArticleAutoDraftCleanupResult>>('/admin/article-auto-drafts/cleanup', payload)
    .then((res) => res.data.data)
}
