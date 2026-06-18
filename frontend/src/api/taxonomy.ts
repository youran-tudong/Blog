import { request, type ApiResult } from './request'

export interface PageResp<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface TaxonomyQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface CategoryItem {
  id: number
  name: string
  slug: string
  description?: string
  sortOrder: number
  status: number
  articleCount: number
  createTime?: string
  updateTime?: string
}

export interface CategoryPayload {
  name: string
  slug: string
  description?: string
  sortOrder: number
  status: number
}

export interface TagItem {
  id: number
  name: string
  slug: string
  color?: string
  articleCount: number
  createTime?: string
  updateTime?: string
}

export interface TagPayload {
  name: string
  slug: string
  color?: string
}

export function pageCategoriesApi(params: TaxonomyQuery) {
  return request.get<ApiResult<PageResp<CategoryItem>>>('/admin/taxonomy/categories', { params }).then((res) => res.data.data)
}

export function createCategoryApi(payload: CategoryPayload) {
  return request.post<ApiResult<CategoryItem>>('/admin/taxonomy/categories', payload).then((res) => res.data.data)
}

export function updateCategoryApi(id: number, payload: CategoryPayload) {
  return request.put<ApiResult<CategoryItem>>(`/admin/taxonomy/categories/${id}`, payload).then((res) => res.data.data)
}

export function deleteCategoryApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/taxonomy/categories/${id}`).then((res) => res.data.data)
}

export function pageTagsApi(params: TaxonomyQuery) {
  return request.get<ApiResult<PageResp<TagItem>>>('/admin/taxonomy/tags', { params }).then((res) => res.data.data)
}

export function createTagApi(payload: TagPayload) {
  return request.post<ApiResult<TagItem>>('/admin/taxonomy/tags', payload).then((res) => res.data.data)
}

export function updateTagApi(id: number, payload: TagPayload) {
  return request.put<ApiResult<TagItem>>(`/admin/taxonomy/tags/${id}`, payload).then((res) => res.data.data)
}

export function deleteTagApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/taxonomy/tags/${id}`).then((res) => res.data.data)
}

export function listPublicCategoriesApi() {
  return request.get<ApiResult<CategoryItem[]>>('/public/taxonomy/categories').then((res) => res.data.data)
}

export function listPublicTagsApi() {
  return request.get<ApiResult<TagItem[]>>('/public/taxonomy/tags').then((res) => res.data.data)
}
