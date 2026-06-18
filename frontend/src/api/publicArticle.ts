import { request, type ApiResult } from './request'
import type { ArticleItem, ArticleListItem } from './article'
import type { PageResp } from './taxonomy'

export interface PublicArticleQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  categoryId?: number
  tagId?: number
  columnId?: number
}

export interface ArchiveMonth {
  month: string
  monthLabel: string
  articles: ArticleListItem[]
}

export interface ArchiveHeatmapDay {
  date: string
  count: number
}

export interface ArchiveHeatmap {
  year: number
  total: number
  days: ArchiveHeatmapDay[]
}

export interface PublicArticleSearchQuery {
  pageNo?: number
  pageSize?: number
  keyword: string
  sort?: 'latest' | 'views'
}

export interface ArticleSearchItem {
  id: number
  title: string
  slug: string
  summary?: string
  snippet?: string
  categoryName?: string
  columnName?: string
  tagNames: string[]
  viewCount: number
  publishTime?: string
  createTime?: string
}

export interface ArticleLikeStatus {
  liked: boolean
  likeCount: number
}

export function pagePublicArticlesApi(params: PublicArticleQuery) {
  return request.get<ApiResult<PageResp<ArticleListItem>>>('/public/articles', { params }).then((res) => res.data.data)
}

export function searchPublicArticlesApi(params: PublicArticleSearchQuery) {
  return request.get<ApiResult<PageResp<ArticleSearchItem>>>('/public/articles/search', { params }).then((res) => res.data.data)
}

export function getPublicArticleApi(slug: string) {
  return request.get<ApiResult<ArticleItem>>(`/public/articles/${slug}`).then((res) => res.data.data)
}

export function listPopularPublicArticlesApi(limit = 5) {
  return request
    .get<ApiResult<ArticleListItem[]>>('/public/articles/popular', { params: { limit } })
    .then((res) => res.data.data)
}

export function listRelatedPublicArticlesApi(slug: string, limit = 4) {
  return request
    .get<ApiResult<ArticleListItem[]>>(`/public/articles/${slug}/related`, { params: { limit } })
    .then((res) => res.data.data)
}

const visitorHeaders = (visitorKey: string) => ({
  'X-Visitor-Key': visitorKey,
})

export function getPublicArticleLikeStatusApi(slug: string, visitorKey: string) {
  return request
    .get<ApiResult<ArticleLikeStatus>>(`/public/articles/${slug}/likes/status`, {
      headers: visitorHeaders(visitorKey),
    })
    .then((res) => res.data.data)
}

export function likePublicArticleApi(slug: string, visitorKey: string) {
  return request
    .post<ApiResult<ArticleLikeStatus>>(`/public/articles/${slug}/likes`, undefined, {
      headers: visitorHeaders(visitorKey),
    })
    .then((res) => res.data.data)
}

export function unlikePublicArticleApi(slug: string, visitorKey: string) {
  return request
    .delete<ApiResult<ArticleLikeStatus>>(`/public/articles/${slug}/likes`, {
      headers: visitorHeaders(visitorKey),
    })
    .then((res) => res.data.data)
}

export function listPublicArchivesApi() {
  return request.get<ApiResult<ArchiveMonth[]>>('/public/articles/archives').then((res) => res.data.data)
}

export function getPublicArchiveHeatmapApi(year?: number) {
  return request
    .get<ApiResult<ArchiveHeatmap>>('/public/articles/archives/heatmap', { params: { year } })
    .then((res) => res.data.data)
}
