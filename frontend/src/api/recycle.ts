import { request, type ApiResult } from './request'
import type { PageResp } from './taxonomy'

export interface RecycleQuery {
  pageNo?: number
  pageSize?: number
  resourceType?: string
  keyword?: string
}

export interface RecycleItem {
  id: number
  resourceType: string
  resourceId: number
  title?: string
  deleteBy?: number
  deleteTime?: string
}

export function pageRecycleApi(params: RecycleQuery) {
  return request.get<ApiResult<PageResp<RecycleItem>>>('/admin/recycle', { params }).then((res) => res.data.data)
}

export function restoreRecycleApi(id: number) {
  return request.post<ApiResult<void>>(`/admin/recycle/${id}/restore`).then((res) => res.data.data)
}

export function deleteRecyclePermanentlyApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/recycle/${id}`).then((res) => res.data.data)
}
