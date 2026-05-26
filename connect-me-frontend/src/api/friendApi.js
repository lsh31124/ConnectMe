import { apiFetch } from './apiFetch'

const BASE = '/friends'

export async function getFriends() {
  const res = await apiFetch(BASE)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 목록 조회에 실패했습니다.')
  }
  return res.json()
}

export async function getFriendRequests() {
  const res = await apiFetch(`${BASE}/requests`)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 요청 목록 조회에 실패했습니다.')
  }
  return res.json()
}

export async function acceptFriend(friendId) {
  const res = await apiFetch(`${BASE}/${friendId}/accept`, { method: 'PATCH' })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 수락에 실패했습니다.')
  }
  return res.json()
}

export async function rejectFriend(friendId) {
  const res = await apiFetch(`${BASE}/${friendId}/reject`, { method: 'PATCH' })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 거절에 실패했습니다.')
  }
}