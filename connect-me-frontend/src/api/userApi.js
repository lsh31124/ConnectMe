import { apiFetch } from './apiFetch'

const BASE = '/users'

export async function getMyProfile() {
  const res = await apiFetch(`${BASE}/me`)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '프로필 조회에 실패했습니다.')
  }
  return res.json()
}

export async function updateMyProfile(data) {
  const res = await apiFetch(`${BASE}/me`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '프로필 수정에 실패했습니다.')
  }
  return res.json()
}

export async function searchUsers(query) {
  const res = await apiFetch(`${BASE}?query=${encodeURIComponent(query)}`)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '사용자 검색에 실패했습니다.')
  }
  return res.json()
}