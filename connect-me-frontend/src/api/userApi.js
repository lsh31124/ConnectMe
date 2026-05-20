import useAuthStore from '../store/useAuthStore'

const BASE = '/users'

function authHeaders() {
  const token = useAuthStore.getState().accessToken
  return { Authorization: `Bearer ${token}` }
}

export async function searchUsers(query) {
  const res = await fetch(`${BASE}?query=${encodeURIComponent(query)}`, {
    headers: authHeaders(),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '사용자 검색에 실패했습니다.')
  }
  return res.json()
}

export async function getMyProfile(accessToken) {
  const res = await fetch(`${BASE}/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '프로필 조회에 실패했습니다.')
  }
  return res.json()
}

export async function updateMyProfile(data, accessToken) {
  const res = await fetch(`${BASE}/me`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '프로필 수정에 실패했습니다.')
  }
  return res.json()
}