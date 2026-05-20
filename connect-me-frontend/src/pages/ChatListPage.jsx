import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import styles from './ChatListPage.module.css'
import { getChatRooms, createDirectRoom } from '../api/chatApi'
import { searchUsers } from '../api/userApi'

function EditIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
  )
}

function SearchIcon() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8" />
      <path d="M21 21l-4.35-4.35" />
    </svg>
  )
}

function PersonPlaceholderIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#fff"
         strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
    </svg>
  )
}

function ChatBubbleIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M8 10H16M8 14H13M7 4H17C18.1046 4 19 4.8954 19 6V14C19 15.1046 18.1046 16 17 16H13L9 20V16H7C5.8954 16 5 15.1046 5 14V6C5 4.8954 5.8954 4 7 4Z" />
    </svg>
  )
}

function ChatsTabIcon({ active }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill={active ? '#4F46E5' : 'none'}
         stroke={active ? '#4F46E5' : '#9CA3AF'} strokeWidth="2"
         strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
    </svg>
  )
}

function FriendsTabIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 00-3-3.87" />
      <path d="M16 3.13a4 4 0 010 7.75" />
    </svg>
  )
}

function HomeTabIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </svg>
  )
}

function MoreTabIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="5" cy="12" r="1" />
      <circle cx="12" cy="12" r="1" />
      <circle cx="19" cy="12" r="1" />
    </svg>
  )
}

function EmptyBubbleIcon() {
  return (
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB"
         strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
    </svg>
  )
}

export default function ChatListPage() {
  const navigate = useNavigate()
  const [chatRooms, setChatRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchQuery, setSearchQuery] = useState('')

  const [showModal, setShowModal] = useState(false)
  const [userQuery, setUserQuery] = useState('')
  const [userResults, setUserResults] = useState([])
  const [searching, setSearching] = useState(false)
  const [creating, setCreating] = useState(false)
  const searchTimer = useRef(null)

  useEffect(() => {
    getChatRooms()
      .then((res) => setChatRooms(res.data ?? []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!userQuery.trim()) { setUserResults([]); return }
    clearTimeout(searchTimer.current)
    searchTimer.current = setTimeout(() => {
      setSearching(true)
      searchUsers(userQuery)
        .then((res) => setUserResults(res.data ?? []))
        .catch(() => setUserResults([]))
        .finally(() => setSearching(false))
    }, 400)
  }, [userQuery])

  const handleStartChat = async (targetUserId) => {
    setCreating(true)
    try {
      await createDirectRoom(targetUserId)
      setShowModal(false)
      setUserQuery('')
      setUserResults([])
      setLoading(true)
      getChatRooms()
        .then((res) => setChatRooms(res.data ?? []))
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false))
    } catch (err) {
      alert(err.message)
    } finally {
      setCreating(false)
    }
  }

  const filtered = (chatRooms ?? []).filter((room) =>
    (room.name ?? '').toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <div className={styles.avatarCircle}>
            <ChatBubbleIcon />
          </div>
          <span className={styles.headerTitle}>Messages</span>
        </div>
        <button className={styles.iconBtn} aria-label="새 채팅" onClick={() => setShowModal(true)}>
          <EditIcon />
        </button>
      </header>

      {showModal && (
        <div className={styles.modalOverlay} onClick={() => setShowModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <span className={styles.modalTitle}>새 채팅 시작</span>
              <button className={styles.modalClose} onClick={() => setShowModal(false)}>✕</button>
            </div>
            <input
              className={styles.modalInput}
              type="text"
              placeholder="이메일 또는 전화번호로 검색"
              value={userQuery}
              onChange={(e) => setUserQuery(e.target.value)}
              autoFocus
            />
            <div className={styles.modalResults}>
              {searching && <p className={styles.modalHint}>검색 중...</p>}
              {!searching && userQuery && userResults.length === 0 && (
                <p className={styles.modalHint}>검색 결과가 없습니다.</p>
              )}
              {userResults.map((user) => (
                <button
                  key={user.id}
                  className={styles.userItem}
                  onClick={() => handleStartChat(user.id)}
                  disabled={creating}
                >
                  <span className={styles.userAvatar}><PersonPlaceholderIcon /></span>
                  <span className={styles.userInfo}>
                    <span className={styles.userName}>{user.name}</span>
                    <span className={styles.userEmail}>{user.email}</span>
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      <div className={styles.searchWrap}>
        <div className={styles.searchInner}>
          <span className={styles.searchIcon}><SearchIcon /></span>
          <input
            type="text"
            className={styles.searchInput}
            placeholder="Search conversations..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      <div className={styles.listWrap}>
        {loading && (
          <div className={styles.loadingWrap}>
            <div className={styles.spinner} />
            <p className={styles.loadingText}>로딩 중...</p>
          </div>
        )}

        {!loading && error && (
          <p className={styles.errorText}>{error}</p>
        )}

        {!loading && !error && filtered.length === 0 && (
          <div className={styles.emptyState}>
            <span className={styles.emptyIcon}><EmptyBubbleIcon /></span>
            <p className={styles.emptyText}>아직 채팅방이 없습니다</p>
          </div>
        )}

        {!loading && !error && filtered.map((room, index) => (
          <div key={room.id}>
            <Link to={`/chats/${room.id}`} className={styles.chatItem}>
              <div className={styles.profileWrap}>
                <div className={styles.profileImg}>
                  {room.profileImage
                    ? <img src={room.profileImage} alt={room.name} />
                    : <PersonPlaceholderIcon />
                  }
                </div>
                {room.isOnline && <span className={styles.onlineDot} />}
              </div>

              <div className={styles.chatInfo}>
                <p className={styles.chatName}>{room.name}</p>
                <p className={styles.lastMessage}>{room.lastMessage}</p>
              </div>

              <div className={styles.chatMeta}>
                <span className={styles.timeText}>{room.lastMessageTime}</span>
                {room.unreadCount > 0 && (
                  <span className={styles.badge}>{room.unreadCount}</span>
                )}
              </div>
            </Link>
            {index < filtered.length - 1 && <div className={styles.divider} />}
          </div>
        ))}
      </div>

      <nav className={styles.tabBar}>
        <Link to="/chats" className={`${styles.tabItem} ${styles.tabItemActive}`}>
          <span className={styles.tabIconWrap}><ChatsTabIcon active /></span>
          CHATS
        </Link>
        <Link to="/friends" className={styles.tabItem}>
          <span className={styles.tabIconWrap}><FriendsTabIcon /></span>
          FRIENDS
        </Link>
        <Link to="/home" className={styles.tabItem}>
          <span className={styles.tabIconWrap}><HomeTabIcon /></span>
          HOME
        </Link>
        <Link to="/more" className={styles.tabItem}>
          <span className={styles.tabIconWrap}><MoreTabIcon /></span>
          MORE
        </Link>
      </nav>
    </div>
  )
}