import { apiFetch } from './apiFetch'

export async function getMessages(roomId, cursorId = null, size = 30) {
  const params = new URLSearchParams({ size })
  if (cursorId) params.append('cursorId', cursorId)
  const res = await apiFetch(`/chat-rooms/${roomId}/messages?${params}`)
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '메시지를 불러오지 못했습니다.')
  }
  return res.json()
}

export async function deleteMessage(messageId) {
  const res = await apiFetch(`/messages/${messageId}`, { method: 'DELETE' })
  if (!res.ok) {
    const err = await res.json()
    throw new Error(err.message || '메시지 삭제에 실패했습니다.')
  }
}