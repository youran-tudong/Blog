import { request, type ApiResult } from './request'
import type { PageResp } from './taxonomy'

export interface OperationLogQuery {
  pageNo?: number
  pageSize?: number
  module?: string
  successFlag?: number
}

export interface OperationLogItem {
  id: number
  userId?: number
  module: string
  operation: string
  requestMethod: string
  requestUri: string
  requestParams?: string
  ip?: string
  userAgent?: string
  successFlag: number
  errorMessage?: string
  createTime?: string
}

export function pageOperationLogsApi(params: OperationLogQuery) {
  return request.get<ApiResult<PageResp<OperationLogItem>>>('/admin/operation-logs', { params }).then((res) => res.data.data)
}
