const VISITOR_KEY_STORAGE = 'technote-visitor-key'
const VISITOR_KEY_PATTERN = /^[A-Za-z0-9-]{16,64}$/

let memoryVisitorKey = ''

const createVisitorKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `visitor-${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}-${Math.random()
    .toString(36)
    .slice(2)}`.slice(0, 64)
}

export function getOrCreateVisitorKey() {
  if (memoryVisitorKey) return memoryVisitorKey

  try {
    const storedKey = localStorage.getItem(VISITOR_KEY_STORAGE)
    if (storedKey && VISITOR_KEY_PATTERN.test(storedKey)) {
      memoryVisitorKey = storedKey
      return memoryVisitorKey
    }
  } catch {
    // 隐私模式下 localStorage 可能不可用，继续使用当前页面内存标识。
  }

  memoryVisitorKey = createVisitorKey()
  try {
    localStorage.setItem(VISITOR_KEY_STORAGE, memoryVisitorKey)
  } catch {
    // 写入失败时仍保留当前页面内存标识，保证本次访问的点赞状态一致。
  }
  return memoryVisitorKey
}

