interface SiteHeadDefaults {
  siteTitle: string
  description?: string
  keywords?: string
}

interface DocumentHeadInput {
  title?: string
  description?: string
  keywords?: string
  canonicalPath?: string
  type?: string
  structuredData?: Record<string, unknown> | null
}

const defaults: SiteHeadDefaults = {
  siteTitle: 'TechNote',
  description: '面向技术创作者的个人内容管理与分享系统',
}

let currentHead: DocumentHeadInput | null = null

export const configureSiteHeadDefaults = (nextDefaults: Partial<SiteHeadDefaults>) => {
  defaults.siteTitle = nextDefaults.siteTitle?.trim() || defaults.siteTitle
  defaults.description = nextDefaults.description?.trim() || defaults.description
  defaults.keywords = nextDefaults.keywords?.trim() || defaults.keywords
  if (currentHead) {
    setDocumentHead(currentHead)
  }
}

export const setDocumentHead = (input: DocumentHeadInput) => {
  currentHead = input
  if (typeof document === 'undefined' || typeof window === 'undefined') {
    return
  }
  const title = buildTitle(input.title)
  const description = input.description?.trim() || defaults.description || ''
  const keywords = input.keywords?.trim() || defaults.keywords || ''
  const canonicalUrl = buildAbsoluteUrl(input.canonicalPath || window.location.pathname)
  document.title = title
  setMeta('name', 'description', description)
  setMeta('name', 'keywords', keywords)
  setMeta('property', 'og:title', title)
  setMeta('property', 'og:description', description)
  setMeta('property', 'og:type', input.type || 'website')
  setMeta('property', 'og:url', canonicalUrl)
  setMeta('name', 'twitter:card', 'summary')
  setMeta('name', 'twitter:title', title)
  setMeta('name', 'twitter:description', description)
  setLink('canonical', canonicalUrl)
  setLink('alternate', buildAbsoluteUrl('/api/rss.xml'), {
    type: 'application/rss+xml',
    title: `${defaults.siteTitle} RSS`,
  })
  setJsonLd(input.structuredData)
}

const buildTitle = (title?: string) => {
  const siteTitle = defaults.siteTitle || 'TechNote'
  const pageTitle = title?.trim()
  if (!pageTitle || pageTitle === siteTitle) {
    return siteTitle
  }
  return `${pageTitle} - ${siteTitle}`
}

const buildAbsoluteUrl = (pathOrUrl: string) => {
  if (typeof window === 'undefined') {
    return pathOrUrl || '/'
  }
  const value = pathOrUrl || '/'
  try {
    return new URL(value, window.location.origin).toString()
  } catch {
    return window.location.origin
  }
}

const setMeta = (attribute: 'name' | 'property', key: string, content: string) => {
  let element = document.head.querySelector<HTMLMetaElement>(`meta[${attribute}="${key}"]`)
  if (!element) {
    element = document.createElement('meta')
    element.setAttribute(attribute, key)
    document.head.appendChild(element)
  }
  element.setAttribute('content', content)
}

const setLink = (rel: string, href: string, attrs: Record<string, string> = {}) => {
  const selector =
    rel === 'alternate' && attrs.type
      ? `link[rel="${rel}"][type="${attrs.type}"]`
      : `link[rel="${rel}"]`
  let element = document.head.querySelector<HTMLLinkElement>(selector)
  if (!element) {
    element = document.createElement('link')
    element.setAttribute('rel', rel)
    document.head.appendChild(element)
  }
  element.setAttribute('href', href)
  Object.entries(attrs).forEach(([key, value]) => element?.setAttribute(key, value))
}

const setJsonLd = (structuredData?: Record<string, unknown> | null) => {
  const id = 'technote-json-ld'
  const existing = document.head.querySelector<HTMLScriptElement>(`script#${id}`)
  if (!structuredData) {
    existing?.remove()
    return
  }
  const element = existing || document.createElement('script')
  element.id = id
  element.type = 'application/ld+json'
  element.textContent = JSON.stringify(structuredData)
  if (!existing) {
    document.head.appendChild(element)
  }
}
