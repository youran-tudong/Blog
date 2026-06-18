const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const CODE_KEYWORDS = new Set([
  'abstract',
  'and',
  'async',
  'await',
  'boolean',
  'break',
  'case',
  'catch',
  'class',
  'const',
  'continue',
  'default',
  'def',
  'delete',
  'do',
  'else',
  'enum',
  'export',
  'extends',
  'false',
  'final',
  'finally',
  'for',
  'from',
  'function',
  'if',
  'implements',
  'import',
  'in',
  'interface',
  'let',
  'new',
  'null',
  'or',
  'package',
  'private',
  'protected',
  'public',
  'return',
  'select',
  'static',
  'switch',
  'this',
  'throw',
  'true',
  'try',
  'type',
  'update',
  'var',
  'void',
  'where',
  'while',
])

const COPY_TEXT = '\u590d\u5236'
const COPIED_TEXT = '\u5df2\u590d\u5236'
const COPY_FAILED_TEXT = '\u590d\u5236\u5931\u8d25'
const CODE_FENCE_PATTERN = /^```\s*([A-Za-z0-9_+#.-]*)?\s*$/

export interface MarkdownHeading {
  id: string
  level: number
  text: string
}

const stripHeadingMarkup = (value: string) =>
  value
    .replace(/!\[([^\]]*)\]\([^)]+\)/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/[`*_~]/g, '')
    .trim()

const hashHeadingText = (value: string) => {
  let hash = 0
  for (let index = 0; index < value.length; index += 1) {
    hash = (hash * 31 + value.charCodeAt(index)) >>> 0
  }
  return hash.toString(36)
}

const normalizeHeadingBase = (value: string) => {
  const normalized = value
    .normalize('NFKD')
    .toLowerCase()
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-+|-+$/g, '')
  return normalized || `section-${hashHeadingText(value)}`
}

const createHeadingIdFactory = () => {
  const used = new Map<string, number>()
  return (text: string) => {
    const base = normalizeHeadingBase(text)
    const count = (used.get(base) || 0) + 1
    used.set(base, count)
    return count === 1 ? base : `${base}-${count}`
  }
}

const normalizeLanguage = (value = '') => {
  const normalized = value.trim().toLowerCase().replace(/^language-/, '')
  if (normalized === 'js') return 'javascript'
  if (normalized === 'ts') return 'typescript'
  if (normalized === 'py') return 'python'
  if (normalized === 'sh' || normalized === 'bash') return 'shell'
  if (normalized === 'c#') return 'csharp'
  return normalized.replace(/[^a-z0-9_+#.-]/g, '')
}

const codeLanguageClass = (language: string) => language.replace(/[^a-z0-9_-]/g, '-')

const isHashCommentLanguage = (language: string) =>
  ['python', 'shell', 'yaml', 'yml', 'properties', 'toml'].includes(language)

const highlightPlainCode = (value: string) => {
  const tokenPattern = /\b(\d+(?:\.\d+)?|[A-Za-z_][A-Za-z0-9_]*)\b/g
  let cursor = 0
  let html = ''

  for (const match of value.matchAll(tokenPattern)) {
    const token = match[0]
    const index = match.index || 0
    html += escapeHtml(value.slice(cursor, index))
    const lowerToken = token.toLowerCase()
    if (/^\d+(?:\.\d+)?$/.test(token)) {
      html += `<span class="code-token-number">${escapeHtml(token)}</span>`
      cursor = index + token.length
      continue
    }
    if (CODE_KEYWORDS.has(lowerToken)) {
      html += `<span class="code-token-keyword">${escapeHtml(token)}</span>`
      cursor = index + token.length
      continue
    }
    html += escapeHtml(token)
    cursor = index + token.length
  }

  html += escapeHtml(value.slice(cursor))
  return html
}

const highlightCodeLine = (line: string, language: string) => {
  let cursor = 0
  let html = ''

  const appendPlain = (end: number) => {
    if (end > cursor) {
      html += highlightPlainCode(line.slice(cursor, end))
      cursor = end
    }
  }

  while (cursor < line.length) {
    const char = line[cursor]
    const rest = line.slice(cursor)
    const isSlashComment = rest.startsWith('//')
    const isHashComment = isHashCommentLanguage(language) && char === '#'

    if (isSlashComment || isHashComment) {
      html += `<span class="code-token-comment">${escapeHtml(rest)}</span>`
      cursor = line.length
      break
    }

    if (char === '"' || char === "'" || char === '`') {
      const quote = char
      let end = cursor + 1
      let escaped = false
      while (end < line.length) {
        const nextChar = line[end]
        if (!escaped && nextChar === quote) {
          end += 1
          break
        }
        escaped = !escaped && nextChar === '\\'
        if (nextChar !== '\\') {
          escaped = false
        }
        end += 1
      }
      html += `<span class="code-token-string">${escapeHtml(line.slice(cursor, end))}</span>`
      cursor = end
      continue
    }

    const nextSpecialIndexes = [
      line.indexOf('//', cursor + 1),
      line.indexOf('"', cursor + 1),
      line.indexOf("'", cursor + 1),
      line.indexOf('`', cursor + 1),
      isHashCommentLanguage(language) ? line.indexOf('#', cursor + 1) : -1,
    ].filter((index) => index >= 0)
    const nextSpecial = nextSpecialIndexes.length > 0 ? Math.min(...nextSpecialIndexes) : line.length
    appendPlain(nextSpecial)
  }

  return html
}

const renderCodeBlock = (code: string, language = '') => {
  const normalizedLanguage = normalizeLanguage(language)
  const label = normalizedLanguage || 'text'
  const languageClass = codeLanguageClass(label)
  const codeRows = code.split('\n').map((line, index) => {
    const emptyLine = line.length === 0
    const lineHtml = emptyLine ? '&nbsp;' : highlightCodeLine(line, normalizedLanguage)
    return `<span class="markdown-code-row"><span class="markdown-code-line-number">${index + 1}</span><span class="markdown-code-line"${
      emptyLine ? ' data-empty="true"' : ''
    }>${lineHtml}</span></span>`
  })

  return `
    <figure class="markdown-code-block" data-language="${escapeHtml(label)}">
      <figcaption class="markdown-code-header">
        <span class="markdown-code-language">${escapeHtml(label)}</span>
        <button type="button" class="markdown-code-copy" data-code-copy="true">${COPY_TEXT}</button>
      </figcaption>
      <pre class="markdown-code-pre"><code class="markdown-code-code language-${escapeHtml(languageClass)}">${codeRows.join('')}</code></pre>
    </figure>
  `
}

const isSafeUrl = (url: string) => {
  const trimmed = url.trim().toLowerCase()
  return trimmed.startsWith('https://') || trimmed.startsWith('http://') || trimmed.startsWith('/')
}

const renderInline = (value: string) => {
  let html = escapeHtml(value)
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_match, alt: string, url: string) => {
    if (!isSafeUrl(url)) {
      return escapeHtml(alt)
    }
    return `<img src="${escapeHtml(url)}" alt="${escapeHtml(alt)}" loading="lazy" />`
  })
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_match, text: string, url: string) => {
    if (!isSafeUrl(url)) {
      return escapeHtml(text)
    }
    return `<a href="${escapeHtml(url)}" target="_blank" rel="noreferrer">${escapeHtml(text)}</a>`
  })
  return html
}

const renderParagraphs = (lines: string[], nextHeadingId?: (text: string) => string) => {
  const html: string[] = []
  let listItems: string[] = []

  const flushList = () => {
    if (listItems.length > 0) {
      html.push(`<ul>${listItems.join('')}</ul>`)
      listItems = []
    }
  }

  for (const line of lines) {
    if (!line.trim()) {
      flushList()
      continue
    }
    const heading = line.match(/^(#{1,4})\s+(.+)$/)
    if (heading) {
      flushList()
      const level = heading[1].length
      const headingText = stripHeadingMarkup(heading[2])
      const headingId = nextHeadingId ? nextHeadingId(headingText) : normalizeHeadingBase(headingText)
      html.push(`<h${level} id="${escapeHtml(headingId)}">${renderInline(heading[2])}</h${level}>`)
      continue
    }
    const quote = line.match(/^>\s+(.+)$/)
    if (quote) {
      flushList()
      html.push(`<blockquote>${renderInline(quote[1])}</blockquote>`)
      continue
    }
    const listItem = line.match(/^[-*]\s+(.+)$/)
    if (listItem) {
      listItems.push(`<li>${renderInline(listItem[1])}</li>`)
      continue
    }
    flushList()
    html.push(`<p>${renderInline(line)}</p>`)
  }

  flushList()
  return html.join('')
}

const writeTextToClipboard = async (text: string) => {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text)
    return
  }

  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'true')
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  textarea.style.top = '0'
  document.body.appendChild(textarea)
  textarea.select()
  const copied = document.execCommand('copy')
  document.body.removeChild(textarea)
  if (!copied) {
    throw new Error('Clipboard copy failed')
  }
}

const flashCopyButtonText = (button: HTMLButtonElement, text: string) => {
  const originalText = button.textContent || COPY_TEXT
  button.textContent = text
  window.setTimeout(() => {
    button.textContent = originalText
  }, 1200)
}

export const extractMarkdownHeadings = (markdown?: string): MarkdownHeading[] => {
  if (!markdown) {
    return []
  }
  const headings: MarkdownHeading[] = []
  const nextHeadingId = createHeadingIdFactory()
  const lines = markdown.replace(/\r\n/g, '\n').split('\n')
  let inCodeBlock = false

  for (const line of lines) {
    const codeFence = line.trim().match(CODE_FENCE_PATTERN)
    if (codeFence) {
      inCodeBlock = !inCodeBlock
      continue
    }
    if (inCodeBlock) {
      continue
    }
    const heading = line.match(/^(#{1,4})\s+(.+)$/)
    if (!heading) {
      continue
    }
    const text = stripHeadingMarkup(heading[2])
    if (!text) {
      continue
    }
    headings.push({
      id: nextHeadingId(text),
      level: heading[1].length,
      text,
    })
  }

  return headings
}

export const renderMarkdown = (markdown?: string) => {
  if (!markdown) {
    return ''
  }
  const html: string[] = []
  const lines = markdown.replace(/\r\n/g, '\n').split('\n')
  const nextHeadingId = createHeadingIdFactory()
  let paragraphLines: string[] = []
  let codeLines: string[] = []
  let codeLanguage = ''
  let inCodeBlock = false

  const flushParagraphs = () => {
    if (paragraphLines.length > 0) {
      html.push(renderParagraphs(paragraphLines, nextHeadingId))
      paragraphLines = []
    }
  }

  for (const line of lines) {
    const codeFence = line.trim().match(CODE_FENCE_PATTERN)
    if (codeFence) {
      if (inCodeBlock) {
        html.push(renderCodeBlock(codeLines.join('\n'), codeLanguage))
        codeLines = []
        codeLanguage = ''
        inCodeBlock = false
      } else {
        flushParagraphs()
        codeLanguage = codeFence[1] || ''
        inCodeBlock = true
      }
      continue
    }
    if (inCodeBlock) {
      codeLines.push(line)
    } else {
      paragraphLines.push(line)
    }
  }

  flushParagraphs()
  if (inCodeBlock) {
    html.push(renderCodeBlock(codeLines.join('\n'), codeLanguage))
  }
  return html.join('')
}

export const copyMarkdownCodeFromEvent = async (event: MouseEvent) => {
  const target = event.target
  if (!(target instanceof Element)) {
    return false
  }
  const button = target.closest<HTMLButtonElement>('[data-code-copy="true"]')
  if (!button) {
    return false
  }

  const codeBlock = button.closest('.markdown-code-block')
  const codeLines = [...(codeBlock?.querySelectorAll<HTMLElement>('.markdown-code-line') || [])]
    .map((line) => (line.dataset.empty === 'true' ? '' : line.textContent || ''))
    .join('\n')
  if (!codeLines) {
    return false
  }

  try {
    await writeTextToClipboard(codeLines)
    flashCopyButtonText(button, COPIED_TEXT)
    return true
  } catch {
    flashCopyButtonText(button, COPY_FAILED_TEXT)
    return false
  }
}
