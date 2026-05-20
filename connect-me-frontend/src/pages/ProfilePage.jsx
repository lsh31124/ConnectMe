import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import styles from './ProfilePage.module.css'
import useAuthStore from '../store/useAuthStore'
import { getMyProfile, updateMyProfile } from '../api/userApi'

function BackIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5M12 5l-7 7 7 7" />
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

function MessageIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
    </svg>
  )
}

function CameraIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z" />
      <circle cx="12" cy="13" r="4" />
    </svg>
  )
}

export default function ProfilePage() {
  const navigate = useNavigate()
  const accessToken = useAuthStore((s) => s.accessToken)

  const [profile, setProfile] = useState(null)
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState('')
  const [statusMessage, setStatusMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')

  useEffect(() => {
    getMyProfile(accessToken)
      .then((res) => {
        setProfile(res.data)
        setName(res.data.name)
        setStatusMessage(res.data.statusMessage || '')
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [accessToken])

  const handleEdit = () => {
    setName(profile.name)
    setStatusMessage(profile.statusMessage || '')
    setError('')
    setSuccessMsg('')
    setEditing(true)
  }

  const handleCancel = () => {
    setEditing(false)
    setError('')
  }

  const handleSave = async () => {
    setError('')
    setSuccessMsg('')
    setSaving(true)
    try {
      const res = await updateMyProfile({ name, statusMessage }, accessToken)
      setProfile(res.data)
      setEditing(false)
      setSuccessMsg('저장되었습니다.')
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const initials = profile?.name?.charAt(0)?.toUpperCase() ?? '?'

  return (
    <div className={styles.container}>
      <div className={styles.card}>

        <div className={styles.header}>
          <button className={styles.backBtn} onClick={() => navigate(-1)} aria-label="뒤로">
            <BackIcon />
          </button>
          <span className={styles.headerTitle}>내 프로필</span>
        </div>

        {loading ? (
          <p className={styles.loadingText}>불러오는 중...</p>
        ) : (
          <>
            <div className={styles.avatarSection}>
              <div className={styles.avatarWrap}>
                <div className={styles.avatar}>
                  {profile?.profileImage
                    ? <img src={profile.profileImage} alt="프로필" className={styles.avatarImg} />
                    : initials
                  }
                </div>
                <div className={styles.avatarEditBtn}>
                  <CameraIcon />
                </div>
              </div>
              <p className={styles.profileName}>{profile?.name}</p>
              <p className={styles.profileEmail}>{profile?.email}</p>
              {profile?.statusMessage
                ? <p className={styles.profileStatus}>{profile.statusMessage}</p>
                : <p className={styles.profileStatusEmpty}>상태메시지를 입력하세요</p>
              }
            </div>

            {editing ? (
              <div className={styles.editSection}>
                <p className={styles.fieldLabel}>이름</p>
                <div className={styles.inputWrap}>
                  <span className={styles.inputIcon}><PersonIcon /></span>
                  <input
                    type="text"
                    className={styles.input}
                    placeholder="이름을 입력하세요"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>

                <p className={styles.fieldLabel}>상태메시지</p>
                <div className={styles.inputWrap}>
                  <span className={styles.inputIcon}><MessageIcon /></span>
                  <input
                    type="text"
                    className={styles.input}
                    placeholder="상태메시지를 입력하세요"
                    value={statusMessage}
                    onChange={(e) => setStatusMessage(e.target.value)}
                  />
                </div>

                {error && <p className={styles.errorText}>{error}</p>}

                <div className={styles.btnRow}>
                  <button className={styles.btnSecondary} onClick={handleCancel}>취소</button>
                  <button className={styles.btnPrimary} onClick={handleSave} disabled={saving}>
                    {saving ? '저장 중...' : '저장'}
                  </button>
                </div>
              </div>
            ) : (
              <div className={styles.viewSection}>
                {successMsg && <p className={styles.successText}>{successMsg}</p>}
                {error && <p className={styles.errorText}>{error}</p>}
                <button className={styles.viewEditBtn} onClick={handleEdit}>편집</button>
              </div>
            )}
          </>
        )}

      </div>
    </div>
  )
}