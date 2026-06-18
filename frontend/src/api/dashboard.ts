import { request, type ApiResult } from './request'

export interface DashboardRecentArticle {
  id: number
  title: string
  slug: string
  status: number
  visibility: number
  viewCount: number
  updateTime?: string
}

export interface DashboardRecentOperation {
  id: number
  userId?: number
  module?: string
  operation?: string
  successFlag: number
  createTime?: string
}

export interface DashboardPopularArticle {
  id: number
  title: string
  slug: string
  viewCount: number
  publishTime?: string
}

export interface DashboardTrendDay {
  date: string
  viewCount: number
  publishCount: number
}

export interface DashboardCategoryDistribution {
  categoryId?: number
  categoryName: string
  articleCount: number
}

export interface DashboardStats {
  articleTotal: number
  publishedArticleCount: number
  draftArticleCount: number
  autoDraftCount: number
  viewTotal: number
  categoryCount: number
  tagCount: number
  columnCount: number
  mediaCount: number
  pendingCommentCount: number
  pendingGuestbookCount: number
  operationLogCount: number
  recentArticles: DashboardRecentArticle[]
  popularArticles: DashboardPopularArticle[]
  trendDays: DashboardTrendDay[]
  categoryDistribution: DashboardCategoryDistribution[]
  recentOperations: DashboardRecentOperation[]
}

export function getDashboardStatsApi() {
  return request.get<ApiResult<DashboardStats>>('/admin/dashboard/stats').then((res) => res.data.data)
}
