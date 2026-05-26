import { useState, useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import styles from './ChatRoomPage.module.css'
import useAuthStore from '../store/useAuthStore'
import { getChatRoomDetail } from '../api/chatApi'
import { getMessages } from '../api/messageApi'

function BackIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M12 5l-7 7 7 7" />
    </svg>
  )
}

function VideoIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="23 7 16 12 23 17 23 7" />
      <rect x="1" y="5" width="15" height="14" rx="2" />
    </svg>
  )
}

function PhoneIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z" />
    </svg>
  )
}

function MoreIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
      <circle cx="12" cy="5" r="1.5" />
      <circle cx="12" cy="12" r="1.5" />
      <circle cx="12" cy="19" r="1.5" />
    </svg>
  )
}

function SendIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="22" y1="2" x2="11" y2="13" />
      <polygon points="22 2 15 22 11 13 2 9 22 2" />
    </svg>
  )
}

function PlusIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="12" y1="5" x2="12" y2="19" />
      <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
  )
}

function EmojiIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <path d="M8 13s1.5 2 4 2 4-2 4-2" />
      <line x1="9" y1="9" x2="9.01" y2="9" strokeWidth="2.5" />
      <line x1="15" y1="9" x2="15.01" y2="9" strokeWidth="2.5" />
    </svg>
  )
}

function getInitial(name) {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

function formatTime(isoString) {
  if (!isoString) return ''
  return new Date(isoString).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

export default function ChatRoomPage() {
  const { roomId } = useParams()
  const navigate = useNavigate()
  const accessToken = useAuthStore((s) => s.accessToken)

  const [roomDetail, setRoomDetail] = useState(null)
  const [messages, setMessages] = useState([])
  const [inputText, setInputText] = useState('')
  const [loading, setLoading] = useState(true)
  const [connected, setConnected] = useState(false)

  const clientRef = useRef(null)
  const bottomRef = useRef(null)

  let myUserId = null
  try {
    if (accessToken) {
      myUserId = Number(JSON.parse(atob(accessToken.split('.')[1])).sub)
    }
  } catch {}

  useEffect(() => {
    Promise.all([
      getChatRoomDetail(roomId),
      getMessages(roomId),
    ]).then(([roomRes, msgRes]) => {
      setRoomDetail(roomRes.data)
      setMessages((msgRes.data?.messages ?? []).reverse())
    }).catch(() => {}).finally(() => setLoading(false))
  }, [roomId])

  useEffect(() => {
    if (!accessToken) return
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      beforeConnect: () => {
        client.connectHeaders = {
          Authorization: `Bearer ${useAuthStore.getState().accessToken}`,
        }
      },
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/sub/chat/${roomId}`, (msg) => {
          const message = JSON.parse(msg.body)
          setMessages((prev) => [...prev, message])
        })
      },
      onDisconnect: () => setConnected(false),
      reconnectDelay: 5000,
    })
    clientRef.current = client
    client.activate()
    return () => client.deactivate()
  }, [roomId, accessToken])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = () => {
    if (!inputText.trim() || !clientRef.current) return
    clientRef.current.publish({
      destination: `/pub/chat/${roomId}`,
      body: JSON.stringify({ type: 'TEXT', content: inputText.trim() }),
    })
    setInputText('')
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button className={styles.backBtn} onClick={() => navigate('/chats')} aria-label="뒤로">
          <BackIcon />
        </button>

        <div className={styles.headerInfo}>
          <div className={styles.avatar}>
            {getInitial(roomDetail?.name)}
          </div>
          <div className={styles.headerTextWrap}>
            <p className={styles.headerName}>{roomDetail?.name ?? '...'}</p>
            <p className={styles.headerStatus}>{connected ? 'Online' : '연결 중...'}</p>
          </div>
        </div>

        <div className={styles.headerActions}>
          <button className={styles.iconBtn} aria-label="영상통화">
            <VideoIcon />
          </button>
          <button className={styles.iconBtn} aria-label="음성통화">
            <PhoneIcon />
          </button>
          <button className={styles.iconBtn} aria-label="더보기">
            <MoreIcon />
          </button>
        </div>
      </header>

      <div className={styles.messageList}>
        {loading && <p className={styles.loadingText}>로딩 중...</p>}

        {messages.length > 0 && (
          <div className={styles.dateDivider}>
            <span className={styles.dateDividerChip}>TODAY</span>
          </div>
        )}

        {messages.map((msg, idx) => {
          const isMine = msg.senderId === myUserId
          return (
            <div
              key={msg.id ?? idx}
              className={`${styles.msgRow} ${isMine ? styles.msgRowMine : ''}`}
            >
              {!isMine && (
                <div className={styles.avatarSmall}>
                  {getInitial(roomDetail?.name)}
                </div>
              )}
              <div className={`${styles.msgContent} ${isMine ? styles.msgContentMine : ''}`}>
                <div
                  className={`${styles.bubble} ${isMine ? styles.bubbleMine : styles.bubbleOther} ${msg.deleted ? styles.bubbleDeleted : ''}`}
                >
                  {msg.deleted ? '삭제된 메시지입니다.' : (msg.content ?? '')}
                </div>
                <p
                  className={styles.timeText}
                  style={{ textAlign: isMine ? 'right' : 'left' }}
                >
                  {formatTime(msg.createdAt)}
                </p>
              </div>
            </div>
          )
        })}

        <div ref={bottomRef} />
      </div>

      <div className={styles.inputBar}>
        <button className={styles.iconBtn} aria-label="첨부">
          <PlusIcon />
        </button>
        <input
          className={styles.inputField}
          type="text"
          placeholder="Type a message..."
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button className={styles.iconBtn} aria-label="이모지">
          <EmojiIcon />
        </button>
        <button className={styles.sendBtn} onClick={handleSend} aria-label="전송">
          <SendIcon />
        </button>
      </div>
    </div>
  )
}