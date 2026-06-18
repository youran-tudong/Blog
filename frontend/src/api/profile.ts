import { request, type ApiResult } from './request'
import type { UserInfo } from './auth'

export interface ProfileUpdatePayload {
  nickname: string
  avatarUrl?: string
  email?: string
  bio?: string
}

export interface PasswordUpdatePayload {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export function getProfileApi() {
  return request.get<ApiResult<UserInfo>>('/admin/profile').then((res) => res.data.data)
}

export function updateProfileApi(payload: ProfileUpdatePayload) {
  return request.put<ApiResult<UserInfo>>('/admin/profile', payload).then((res) => res.data.data)
}

export function updatePasswordApi(payload: PasswordUpdatePayload) {
  return request.put<ApiResult<void>>('/admin/profile/password', payload).then((res) => res.data.data)
}

