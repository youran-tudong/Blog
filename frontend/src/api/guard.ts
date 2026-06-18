import { request, type ApiResult } from './request'

export interface CaptchaItem {
  captchaId: string
  question: string
  expireSeconds: number
}

export interface CaptchaPayload {
  captchaId?: string
  captchaAnswer?: string
}

export interface PublicSubmitGuardSetting {
  id?: number
  enabled: boolean
  captchaEnabled: boolean
  captchaTtlSeconds: number
  captchaMaxEntries: number
  sensitiveWordEnabled: boolean
  sensitiveWordMessage?: string
  sensitiveWords?: string
  createTime?: string
  updateTime?: string
}

export type PublicSubmitGuardSettingPayload = Omit<PublicSubmitGuardSetting, 'id' | 'createTime' | 'updateTime'>

export function createCaptchaApi() {
  return request.get<ApiResult<CaptchaItem>>('/public/captcha').then((res) => res.data.data)
}

export function getPublicSubmitGuardSettingApi() {
  return request
    .get<ApiResult<PublicSubmitGuardSetting>>('/admin/public-submit-guard')
    .then((res) => res.data.data)
}

export function updatePublicSubmitGuardSettingApi(payload: PublicSubmitGuardSettingPayload) {
  return request
    .put<ApiResult<PublicSubmitGuardSetting>>('/admin/public-submit-guard', payload)
    .then((res) => res.data.data)
}
