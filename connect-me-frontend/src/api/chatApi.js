import { apiFetch } from './apiFetch'

const BASE = '/chat-rooms'

export async function getChatRooms() {
  const res = await apiFetch(BASE)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 목록을 불러오지 못했습니다.')
  }
  return res.json()
}

export async function getChatRoomDetail(roomId) {
  const res = await apiFetch(`${BASE}/${roomId}`)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 정보를 불러오지 못했습니다.')
  }
  return res.json()
}

export async function createDirectRoom(targetUserId) {
  const res = await apiFetch(`${BASE}/direct`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ targetUserId }),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 생성에 실패했습니다.')
  }
  return res.json()
}

export async function createGroupRoom({ name, memberIds }) {
  const res = await apiFetch(`${BASE}/group`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memberIds }),
  })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '그룹 채팅방 생성에 실패했습니다.')
  }
  return res.json()
}

export async function leaveChatRoom(roomId) {
  const res = await apiFetch(`${BASE}/${roomId}/members/me`, { method: 'DELETE' })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '채팅방 나가기에 실패했습니다.')
  }
}