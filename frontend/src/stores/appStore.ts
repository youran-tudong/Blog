import { defineStore } from 'pinia'
import type { UserInfo } from '../api/auth'

export type ThemeMode = 'light' | 'dark' | 'system'

const applyTheme = (theme: ThemeMode) => {
  const root = document.documentElement
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const dark = theme === 'dark' || (theme === 'system' && systemDark)
  root.classList.toggle('dark', dark)
}

const initialTheme = (localStorage.getItem('technote-theme') as ThemeMode | null) || 'system'
applyTheme(initialTheme)

export const useAppStore = defineStore('app', {
  state: () => ({
    theme: initialTheme as ThemeMode,
    token: localStorage.getItem('technote-token') || '',
    user: undefined as UserInfo | undefined,
  }),
  actions: {
    setTheme(theme: ThemeMode) {
      localStorage.setItem('technote-theme', theme)
      applyTheme(theme)
      this.theme = theme
    },
    setToken(token?: string) {
      if (token) {
        localStorage.setItem('technote-token', token)
      } else {
        localStorage.removeItem('technote-token')
      }
      this.token = token || ''
    },
    setUser(user?: UserInfo) {
      this.user = user
    },
    clearAuth() {
      localStorage.removeItem('technote-token')
      this.token = ''
      this.user = undefined
    },
  },
})

