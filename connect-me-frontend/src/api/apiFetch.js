import { refreshTokens, logout } from './authApi'
import useAuthStore from '../store/useAuthStore'

let isRefreshing = false
let queue = []

function flushQueue(newToken) {
  queue.forEach((resolve) => resolve(newToken))
  queue = []
}

function withAuth(options, token) {
  if (!token) return options
  return {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${token}`,
    },
  }
}

export async function apiFetch(url, options = {}) {
  const { accessToken } = useAuthStore.getState()
  const res = await fetch(url, withAuth(options, accessToken))

  if (res.status !== 403) return res

  const { refreshToken } = useAuthStore.getState()

  if (isRefreshing) {
    return new Promise((resolve) => {
      queue.push((newToken) => {
        resolve(fetch(url, withAuth(options, newToken)))
      })
    })
  }

  isRefreshing = true

  try {
    const data = await refreshTokens(refreshToken)
    const { accessToken: newAccess, refreshToken: newRefresh } = data.data
    useAuthStore.setState({ accessToken: newAccess, refreshToken: newRefresh })
    isRefreshing = false
    flushQueue(newAccess)
    return fetch(url, withAuth(options, newAccess))
  } catch {
    isRefreshing = false
    flushQueue(null)
    await logout(refreshToken)
    useAuthStore.setState({ accessToken: null, refreshToken: null, isAuthenticated: false })
    throw new Error('세션이 만료되었습니다. 다시 로그인해 주세요.')
  }
}