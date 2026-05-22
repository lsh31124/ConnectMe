import { useNavigate, Link } from 'react-router-dom'
import useAuthStore from '../store/useAuthStore'
import styles from './SettingsPage.module.css'

function ChevronIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 18l6-6-6-6" />
    </svg>
  )
}

function PersonIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0110 0v4" />
    </svg>
  )
}

function BellIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.73 21a2 2 0 01-3.46 0" />
    </svg>
  )
}

function LogoutIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
      <polyline points="16 17 21 12 16 7" />
      <line x1="21" y1="12" x2="9" y2="12" />
    </svg>
  )
}

export default function SettingsPage() {
  const navigate = useNavigate()
  const logoutAction = useAuthStore((s) => s.logoutAction)

  const handleLogout = async () => {
    await logoutAction()
    navigate('/login', { replace: true })
  }

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <span className={styles.headerTitle}>설정</span>
      </header>

      <section className={styles.section}>
        <p className={styles.sectionLabel}>계정</p>
        <Link to="/profile" className={styles.menuItem}>
          <span className={styles.menuIcon}><PersonIcon /></span>
          <span className={styles.menuText}>내 프로필</span>
          <span className={styles.menuArrow}><ChevronIcon /></span>
        </Link>
        <div className={styles.menuItem} style={{ cursor: 'default', opacity: 0.4 }}>
          <span className={styles.menuIcon}><LockIcon /></span>
          <span className={styles.menuText}>개인정보 보호</span>
          <span className={styles.menuArrow}><ChevronIcon /></span>
        </div>
      </section>

      <section className={styles.section}>
        <p className={styles.sectionLabel}>앱</p>
        <div className={styles.menuItem} style={{ cursor: 'default', opacity: 0.4 }}>
          <span className={styles.menuIcon}><BellIcon /></span>
          <span className={styles.menuText}>알림</span>
          <span className={styles.menuArrow}><ChevronIcon /></span>
        </div>
      </section>

      <section className={styles.section}>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          <span className={styles.menuIcon}><LogoutIcon /></span>
          <span className={styles.logoutText}>로그아웃</span>
        </button>
      </section>

      <nav className={styles.tabBar}>
        <Link to="/chats" className={styles.tabItem}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
          </svg>
          CHATS
        </Link>
        <Link to="/friends" className={styles.tabItem}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 00-3-3.87" />
            <path d="M16 3.13a4 4 0 010 7.75" />
          </svg>
          FRIENDS
        </Link>
        <Link to="/chats" className={styles.tabItem}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
          HOME
        </Link>
        <Link to="/settings" className={`${styles.tabItem} ${styles.tabItemActive}`}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#4F46E5" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="5" cy="12" r="1" />
            <circle cx="12" cy="12" r="1" />
            <circle cx="19" cy="12" r="1" />
          </svg>
          MORE
        </Link>
      </nav>
    </div>
  )
}