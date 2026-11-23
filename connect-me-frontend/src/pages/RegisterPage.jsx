import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import styles from './RegisterPage.module.css'
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

function PersonIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
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

export default function RegisterPage() {
  const navigate = useNavigate()
  const registerAction = useAuthStore((s) => s.registerAction)

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const validate = () => {
    if (!name.trim() || !email.trim() || !password.trim()) {
      setError('모든 항목을 입력해주세요.')
      return false
    }
    if (password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.')
      return false
    }
    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!validate()) return

    setLoading(true)
    try {
      await registerAction({ name, email, password })
      navigate('/login')
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
          <p className={styles.subtitle}>Create your account</p>

          <form onSubmit={handleSubmit} noValidate>
            <p className={styles.fieldLabel}>Name</p>
            <div className={styles.inputWrap}>
              <span className={styles.inputIcon}><PersonIcon /></span>
              <input
                type="text"
                className={styles.input}
                placeholder="이름을 입력하세요"
                value={name}
                onChange={(e) => setName(e.target.value)}
                autoComplete="name"
              />
            </div>

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
                placeholder="비밀번호 8자 이상"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="new-password"
              />
            </div>

            {error && <p className={styles.errorText}>{error}</p>}

            <button type="submit" className={styles.btnPrimary} disabled={loading}>
              {loading ? 'Loading...' : 'Create Account'}
            </button>
          </form>

          <p className={styles.signupText}>
            Already have an account?{' '}
            <Link to="/login" className={styles.link}>Sign in</Link>
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