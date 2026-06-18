import { request, type ApiResult } from './request'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
  email?: string
  bio?: string
  roleCode: string
}

export interface LoginResp {
  tokenName: string
  tokenValue: string
  userInfo: UserInfo
}

export function loginApi(payload: { username: string; password: string }) {
  return request.post<ApiResult<LoginResp>>('/auth/login', payload).then((res) => res.data.data)
}

export function getCurrentUserApi() {
  return request.get<ApiResult<UserInfo>>('/auth/me').then((res) => res.data.data)
}

export function logoutApi() {
  return request.post<ApiResult<void>>('/auth/logout').then((res) => res.data.data)
}
