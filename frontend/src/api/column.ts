import { request, type ApiResult } from './request'
import type { ArticleListItem } from './article'
import type { PageResp } from './taxonomy'

export interface ColumnQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface ColumnItem {
  id: number
  name: string
  slug: string
  description?: string
  coverUrl?: string
  sortOrder: number
  status: number
  articleCount: number
  createTime?: string
  updateTime?: string
}

export interface ColumnPayload {
  name: string
  slug: string
  description?: string
  coverUrl?: string
  sortOrder: number
  status: number
}

export interface ColumnArticlePayload {
  articleId: number
  sortOrder?: number
}

export interface ColumnArticleItem {
  articleId: number
  title: string
  slug: string
  status: number
  visibility: number
  sortOrder: number
  publishTime?: string
  updateTime?: string
}

export function pageColumnsApi(params: ColumnQuery) {
  return request.get<ApiResult<PageResp<ColumnItem>>>('/admin/columns', { params }).then((res) => res.data.data)
}

export function createColumnApi(payload: ColumnPayload) {
  return request.post<ApiResult<ColumnItem>>('/admin/columns', payload).then((res) => res.data.data)
}

export function updateColumnApi(id: number, payload: ColumnPayload) {
  return request.put<ApiResult<ColumnItem>>(`/admin/columns/${id}`, payload).then((res) => res.data.data)
}

export function updateColumnSortApi(id: number, sortOrder: number) {
  return request.put<ApiResult<void>>(`/admin/columns/${id}/sort`, { sortOrder }).then((res) => res.data.data)
}

export function deleteColumnApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/columns/${id}`).then((res) => res.data.data)
}

export function listColumnArticlesApi(id: number) {
  return request.get<ApiResult<ColumnArticleItem[]>>(`/admin/columns/${id}/articles`).then((res) => res.data.data)
}

export function addArticleToColumnApi(id: number, payload: ColumnArticlePayload) {
  return request.post<ApiResult<void>>(`/admin/columns/${id}/articles`, payload).then((res) => res.data.data)
}

export function updateColumnArticleSortApi(id: number, articleId: number, sortOrder: number) {
  return request
    .put<ApiResult<void>>(`/admin/columns/${id}/articles/${articleId}/sort`, { sortOrder })
    .then((res) => res.data.data)
}

export function removeArticleFromColumnApi(id: number, articleId: number) {
  return request.delete<ApiResult<void>>(`/admin/columns/${id}/articles/${articleId}`).then((res) => res.data.data)
}

export function listPublicColumnsApi() {
  return request.get<ApiResult<ColumnItem[]>>('/public/columns').then((res) => res.data.data)
}

export function getPublicColumnApi(slug: string) {
  return request.get<ApiResult<ColumnItem>>(`/public/columns/${slug}`).then((res) => res.data.data)
}

export function pagePublicColumnArticlesApi(slug: string, params: { pageNo?: number; pageSize?: number; keyword?: string }) {
  return request
    .get<ApiResult<PageResp<ArticleListItem>>>(`/public/columns/${slug}/articles`, { params })
    .then((res) => res.data.data)
}
