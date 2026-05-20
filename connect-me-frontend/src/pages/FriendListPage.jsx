import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import styles from './FriendListPage.module.css'
import useAuthStore from '../store/useAuthStore'
import { getFriends, getFriendRequests, acceptFriend, rejectFriend } from '../api/friendApi'

function BackIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M12 5l-7 7 7 7" />
    </svg>
  )
}

export default function FriendListPage() {
  const navigate = useNavigate()
  const accessToken = useAuthStore((s) => s.accessToken)

  const [activeTab, setActiveTab] = useState('friends')
  const [friends, setFriends] = useState([])
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState(null)

  useEffect(() => {
    Promise.all([
      getFriends(accessToken),
      getFriendRequests(accessToken),
    ])
      .then(([friendsRes, requestsRes]) => {
        setFriends(friendsRes.data)
        setRequests(requestsRes.data)
      })
      .finally(() => setLoading(false))
  }, [accessToken])

  const handleAccept = async (friendId) => {
    setProcessingId(friendId)
    try {
      await acceptFriend(friendId, accessToken)
      setRequests((prev) => prev.filter((r) => r.id !== friendId))
    } finally {
      setProcessingId(null)
    }
  }

  const handleReject = async (friendId) => {
    setProcessingId(friendId)
    try {
      await rejectFriend(friendId, accessToken)
      setRequests((prev) => prev.filter((r) => r.id !== friendId))
    } finally {
      setProcessingId(null)
    }
  }

  const initial = (name) => name?.charAt(0)?.toUpperCase() ?? '?'

  return (
    <div className={styles.container}>
      <div className={styles.card}>

        <div className={styles.header}>
          <button className={styles.backBtn} onClick={() => navigate(-1)} aria-label="뒤로">
            <BackIcon />
          </button>
          <span className={styles.headerTitle}>친구</span>
        </div>

        <div className={styles.tabs}>
          <button
            className={`${styles.tabBtn} ${activeTab === 'friends' ? styles.tabBtnActive : ''}`}
            onClick={() => setActiveTab('friends')}
          >
            친구 목록
          </button>
          <button
            className={`${styles.tabBtn} ${activeTab === 'requests' ? styles.tabBtnActive : ''}`}
            onClick={() => setActiveTab('requests')}
          >
            친구 요청
          </button>
        </div>

        <div className={styles.list}>
          {loading ? (
            <p className={styles.loadingMsg}>불러오는 중...</p>
          ) : activeTab === 'friends' ? (
            friends.length === 0 ? (
              <p className={styles.emptyMsg}>친구가 없습니다</p>
            ) : (
              friends.map((f) => (
                <div key={f.id} className={styles.item}>
                  <div className={styles.avatar}>{initial(f.name)}</div>
                  <div className={styles.info}>
                    <p className={styles.name}>{f.name}</p>
                    <p className={styles.email}>{f.email}</p>
                  </div>
                </div>
              ))
            )
          ) : (
            requests.length === 0 ? (
              <p className={styles.emptyMsg}>받은 친구 요청이 없습니다</p>
            ) : (
              requests.map((r) => (
                <div key={r.id} className={styles.item}>
                  <div className={styles.avatar}>{initial(r.name)}</div>
                  <div className={styles.info}>
                    <p className={styles.name}>{r.name}</p>
                    <p className={styles.email}>{r.email}</p>
                  </div>
                  <div className={styles.actions}>
                    <button
                      className={styles.btnAccept}
                      onClick={() => handleAccept(r.id)}
                      disabled={processingId === r.id}
                    >
                      수락
                    </button>
                    <button
                      className={styles.btnReject}
                      onClick={() => handleReject(r.id)}
                      disabled={processingId === r.id}
                    >
                      거절
                    </button>
                  </div>
                </div>
              ))
            )
          )}
        </div>

      </div>
    </div>
  )
}