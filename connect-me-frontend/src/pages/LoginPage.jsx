import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import styles from './LoginPage.module.css'
import useAuthStore from '../store/useAuthStore'

function ChatIcon() {
  return (
    <svg width="36" height="36" viewBox="0 0 24 24" fill="none">
      <path
        d="M8 10H16M8 14H13M7 4H17C18.1046 4 19 4.8954 19 6V14C19 15.1046 18.1046 16 17 16H13L9 20V16H7C5.8954 16 5 15.1046 5 14V6C5 4.8954 5.8954 4 7 4Z"
        stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
      />
    </svg>
  )
}

function MailIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="4" width="20" height="16" rx="2" />
      <path d="M2 7l10 7 10-7" />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="5" y="11" width="14" height="10" rx="2" />
      <path d="M8 11V7a4 4 0 018 0v4" />
    </svg>
  )
}

function GoogleIcon() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
      <path
        d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 8L12 13L4 8V6L12 11L20 6V8Z"
        fill="#6B7280"
      />
    </svg>
  )
}

function AppleIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="#6B7280">
      <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z" />
    </svg>
  )
}

export default function LoginPage() {
  const navigate = useNavigate()
  const loginAction = useAuthStore((s) => s.loginAction)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!email.trim() || !password.trim()) {
      setError('이메일과 비밀번호를 입력해주세요.')
      return
    }
    setLoading(true)
    try {
      await loginAction({ email, password })
      navigate('/chats')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>

        <div className={styles.hero} />

        <div className={styles.content}>

          <div className={styles.iconWrap}>
            <div className={styles.appIcon}><ChatIcon /></div>
          </div>

          <h1 className={styles.title}>Connect Me</h1>
          <p className={styles.subtitle}>The world's most secure messaging app</p>

          <form onSubmit={handleSubmit} noValidate>
            <p className={styles.fieldLabel}>Email</p>
            <div className={styles.inputWrap}>
              <span className={styles.inputIcon}><MailIcon /></span>
              <input
                type="email"
                className={styles.input}
                placeholder="이메일을 입력하세요"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
              />
            </div>

            <p className={styles.fieldLabel}>Password</p>
            <div className={styles.inputWrap}>
              <span className={styles.inputIcon}><LockIcon /></span>
              <input
                type="password"
                className={styles.input}
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>

            {error && <p className={styles.errorText}>{error}</p>}

            <button type="submit" className={styles.btnPrimary} disabled={loading}>
              {loading ? 'Loading...' : 'Continue'}
            </button>
          </form>

          <div className={styles.divider}>
            <span className={styles.dividerLine} />
            <span className={styles.dividerText}>OR CONNECT WITH</span>
            <span className={styles.dividerLine} />
          </div>

          <div className={styles.socialButtons}>
            <button type="button" className={styles.btnSocial}>
              <GoogleIcon /> Google
            </button>
            <button type="button" className={styles.btnSocial}>
              <AppleIcon /> Apple
            </button>
          </div>

          <p className={styles.signupText}>
            New to Connect Me?{' '}
            <Link to="/register" className={styles.link}>Create an account</Link>
          </p>

          <p className={styles.footerText}>
            By continuing, you agree to our{' '}
            <a href="/terms" className={styles.footerLink}>Terms of Service</a>
            {' '}and{' '}
            <a href="/privacy" className={styles.footerLink}>Privacy Policy</a>
          </p>

        </div>
      </div>
    </div>
  )
}