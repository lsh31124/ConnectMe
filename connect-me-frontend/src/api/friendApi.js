const BASE = '/friends'

export async function getFriends(accessToken) {
  const res = await fetch(BASE, {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 목록 조회에 실패했습니다.')
  }
  return res.json()
}

export async function getFriendRequests(accessToken) {
  const res = await fetch(`${BASE}/requests`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 요청 목록 조회에 실패했습니다.')
  }
  return res.json()
}

export async function acceptFriend(friendId, accessToken) {
  const res = await fetch(`${BASE}/${friendId}/accept`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 수락에 실패했습니다.')
  }
  return res.json()
}

export async function rejectFriend(friendId, accessToken) {
  const res = await fetch(`${BASE}/${friendId}/reject`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '친구 거절에 실패했습니다.')
  }
}