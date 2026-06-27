import { request, type ApiResult } from './request'

export interface SettingItem {
  id?: number
  siteTitle: string
  siteDescription?: string
  siteKeywords?: string
  icpNo?: string
  authorName?: string
  authorAvatar?: string
  authorProfile?: string
  announcement?: string
  aboutContent?: string
  createTime?: string
  updateTime?: string
}

export type PublicSettingItem = Omit<SettingItem, 'id' | 'createTime' | 'updateTime'>

export type SettingPayload = Omit<SettingItem, 'id' | 'createTime' | 'updateTime'>

export function getAdminSettingApi() {
  return request.get<ApiResult<SettingItem>>('/admin/settings').then((res) => res.data.data)
}

export function updateAdminSettingApi(payload: SettingPayload) {
  return request.put<ApiResult<SettingItem>>('/admin/settings', payload).then((res) => res.data.data)
}

export function getPublicSettingApi() {
  return request.get<ApiResult<PublicSettingItem>>('/public/settings').then((res) => res.data.data)
}
