import useAuthStore from '../store/useAuthStore'
import { useNavigate } from 'react-router-dom'

export default function HomePage() {
  const logoutAction = useAuthStore((s) => s.logoutAction)
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logoutAction()
    navigate('/login')
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', gap: 16 }}>
      <h1 style={{ fontSize: 24, fontWeight: 700, color: '#111827' }}>Connect Me</h1>
      <p style={{ color: '#6B7280' }}>채팅 기능은 준비 중입니다.</p>
      <button
        onClick={handleLogout}
        style={{ padding: '10px 24px', background: '#4F46E5', color: '#fff', border: 'none', borderRadius: 10, fontSize: 15, fontWeight: 600, cursor: 'pointer' }}
      >
        로그아웃
      </button>
    </div>
  )
}