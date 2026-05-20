import useAuthStore from '../store/useAuthStore'

const BASE = '/chat-rooms'

function authHeaders() {
  const token = useAuthStore.getState().accessToken
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export async function getChatRooms() {
  const res = await fetch(BASE, { headers: authHeaders() })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 목록을 불러오지 못했습니다.')
  }
  return res.json()
}

export async function getChatRoomDetail(roomId) {
  const res = await fetch(`${BASE}/${roomId}`, { headers: authHeaders() })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 정보를 불러오지 못했습니다.')
  }
  return res.json()
}

export async function createDirectRoom(targetUserId) {
  const res = await fetch(`${BASE}/direct`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ targetUserId }),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 생성에 실패했습니다.')
  }
  return res.json()
}

export async function createGroupRoom({ name, memberIds }) {
  const res = await fetch(`${BASE}/group`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ name, memberIds }),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '그룹 채팅방 생성에 실패했습니다.')
  }
  return res.json()
}

export async function leaveChatRoom(roomId) {
  const res = await fetch(`${BASE}/${roomId}/members/me`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 나가기에 실패했습니다.')
  }
}