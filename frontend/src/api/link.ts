import { request, type ApiResult } from './request'
import type { CaptchaPayload } from './guard'
import type { PageResp } from './taxonomy'

export interface LinkItem {
  id: number
  siteName: string
  siteUrl: string
  iconUrl?: string
  description?: string
  sortOrder: number
  status: number
  createTime?: string
  updateTime?: string
}

export interface LinkPayload {
  siteName: string
  siteUrl: string
  iconUrl?: string
  description?: string
  sortOrder: number
  status: number
}

export interface LinkApplyItem {
  id: number
  siteName: string
  siteUrl: string
  iconUrl?: string
  description?: string
  applicantEmail: string
  status: number
  auditRemark?: string
  auditBy?: number
  auditTime?: string
  createTime?: string
  updateTime?: string
}

export interface PublicLinkApplyItem {
  id: number
  siteName: string
  siteUrl: string
  iconUrl?: string
  description?: string
  createTime?: string
}

export interface LinkApplyPayload extends CaptchaPayload {
  siteName: string
  siteUrl: string
  iconUrl?: string
  description?: string
  applicantEmail: string
}

export interface LinkApplyAuditPayload {
  status: number
  auditRemark?: string
}

export interface LinkApplyQuery {
  pageNo?: number
  pageSize?: number
  status?: number
}

export function listAdminLinksApi() {
  return request.get<ApiResult<LinkItem[]>>('/admin/links').then((res) => res.data.data)
}

export function createLinkApi(payload: LinkPayload) {
  return request.post<ApiResult<LinkItem>>('/admin/links', payload).then((res) => res.data.data)
}

export function updateLinkApi(id: number, payload: LinkPayload) {
  return request.put<ApiResult<LinkItem>>(`/admin/links/${id}`, payload).then((res) => res.data.data)
}

export function deleteLinkApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/links/${id}`).then((res) => res.data.data)
}

export function listPublicLinksApi() {
  return request.get<ApiResult<LinkItem[]>>('/public/links').then((res) => res.data.data)
}

export function submitLinkApplyApi(payload: LinkApplyPayload) {
  return request.post<ApiResult<PublicLinkApplyItem>>('/public/link-applies', payload).then((res) => res.data.data)
}

export function pageAdminLinkAppliesApi(params: LinkApplyQuery) {
  return request
    .get<ApiResult<PageResp<LinkApplyItem>>>('/admin/link-applies', { params })
    .then((res) => res.data.data)
}

export function auditLinkApplyApi(id: number, payload: LinkApplyAuditPayload) {
  return request.put<ApiResult<void>>(`/admin/link-applies/${id}/audit`, payload).then((res) => res.data.data)
}

export function deleteLinkApplyApi(id: number) {
  return request.delete<ApiResult<void>>(`/admin/link-applies/${id}`).then((res) => res.data.data)
}
