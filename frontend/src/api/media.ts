import { request, type ApiResult } from './request'
import type { PageResp } from './taxonomy'

export interface MediaItem {
  id: number
  originalName: string
  fileName: string
  filePath: string
  fileSize: number
  mimeType?: string
  fileExt?: string
  quoteCount: number
  uploadBy?: number
  createTime?: string
}

export function uploadImageApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResult<MediaItem>>('/admin/media/images', formData).then((res) => res.data.data)
}

export function pageMediaApi(params: { pageNo?: number; pageSize?: number; keyword?: string }) {
  return request.get<ApiResult<PageResp<MediaItem>>>('/admin/media', { params }).then((res) => res.data.data)
}

export function deleteMediaApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/media/${id}`).then((res) => res.data.data)
}
