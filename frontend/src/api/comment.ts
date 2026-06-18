import { request, type ApiResult } from './request'
import type { CaptchaPayload } from './guard'
import type { PageResp } from './taxonomy'

export interface CommentItem {
  id: number
  articleId: number
  articleTitle?: string
  parentId: number
  nickname: string
  email?: string
  website?: string
  content: string
  status: number
  replyContent?: string
  auditBy?: number
  auditTime?: string
  createTime?: string
  updateTime?: string
}

export interface PublicCommentItem {
  id: number
  articleId: number
  parentId: number
  nickname: string
  website?: string
  content: string
  replyContent?: string
  createTime?: string
  replies: PublicCommentItem[]
}

export interface CommentPayload extends CaptchaPayload {
  articleId: number
  parentId?: number
  nickname: string
  email?: string
  website?: string
  content: string
}

export interface GuestbookItem {
  id: number
  nickname: string
  email?: string
  content: string
  status: number
  replyContent?: string
  auditBy?: number
  auditTime?: string
  createTime?: string
  updateTime?: string
}

export interface GuestbookPayload extends CaptchaPayload {
  nickname: string
  email?: string
  content: string
}

export interface AuditPayload {
  status: number
  replyContent?: string
}

export function listPublicCommentsApi(articleId: number) {
  return request
    .get<ApiResult<PublicCommentItem[]>>('/public/comments', { params: { articleId } })
    .then((res) => res.data.data)
}

export function submitCommentApi(payload: CommentPayload) {
  return request.post<ApiResult<PublicCommentItem>>('/public/comments', payload).then((res) => res.data.data)
}

export function pageAdminCommentsApi(params: { pageNo?: number; pageSize?: number; status?: number }) {
  return request.get<ApiResult<PageResp<CommentItem>>>('/admin/comments', { params }).then((res) => res.data.data)
}

export function auditCommentApi(id: number, payload: AuditPayload) {
  return request.put<ApiResult<void>>(`/admin/comments/${id}/audit`, payload).then((res) => res.data.data)
}

export function deleteCommentApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/comments/${id}`).then((res) => res.data.data)
}

export function listPublicGuestbooksApi() {
  return request.get<ApiResult<GuestbookItem[]>>('/public/guestbooks').then((res) => res.data.data)
}

export function submitGuestbookApi(payload: GuestbookPayload) {
  return request.post<ApiResult<GuestbookItem>>('/public/guestbooks', payload).then((res) => res.data.data)
}

export function pageAdminGuestbooksApi(params: { pageNo?: number; pageSize?: number; status?: number }) {
  return request.get<ApiResult<PageResp<GuestbookItem>>>('/admin/guestbooks', { params }).then((res) => res.data.data)
}

export function auditGuestbookApi(id: number, payload: AuditPayload) {
  return request.put<ApiResult<void>>(`/admin/guestbooks/${id}/audit`, payload).then((res) => res.data.data)
}

export function deleteGuestbookApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/guestbooks/${id}`).then((res) => res.data.data)
}
