import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import useAuthStore from '../store/useAuthStore'
import * as authApi from '../api/authApi'

vi.mock('../api/authApi')

beforeEach(() => {
  localStorage.clear()
  useAuthStore.setState({ accessToken: null, refreshToken: null, isAuthenticated: false })
  vi.resetAllMocks()
})

describe('useAuthStore', () => {
  it('초기 상태는 비인증이다', () => {
    const { result } = renderHook(() => useAuthStore())
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.accessToken).toBeNull()
  })

  it('loginAction 성공 시 토큰을 저장하고 인증 상태가 된다', async () => {
    authApi.login.mockResolvedValue({ data: { accessToken: 'access-tk', refreshToken: 'refresh-tk' } })

    const { result } = renderHook(() => useAuthStore())
    await act(async () => {
      await result.current.loginAction({ email: 'test@email.com', password: 'password123' })
    })

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.accessToken).toBe('access-tk')
    expect(result.current.refreshToken).toBe('refresh-tk')
  })

  it('registerAction 성공 시 토큰을 저장하고 인증 상태가 된다', async () => {
    authApi.register.mockResolvedValue({ data: { accessToken: 'access-tk', refreshToken: 'refresh-tk' } })

    const { result } = renderHook(() => useAuthStore())
    await act(async () => {
      await result.current.registerAction({ email: 'new@email.com', password: 'password123', name: '홍길동' })
    })

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.accessToken).toBe('access-tk')
  })

  it('logoutAction 호출 시 토큰을 삭제하고 비인증 상태가 된다', async () => {
    useAuthStore.setState({ accessToken: 'access-tk', refreshToken: 'refresh-tk', isAuthenticated: true })
    authApi.logout.mockResolvedValue(undefined)

    const { result } = renderHook(() => useAuthStore())
    await act(async () => {
      await result.current.logoutAction()
    })

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.accessToken).toBeNull()
    expect(authApi.logout).toHaveBeenCalledWith('refresh-tk')
  })

  it('loginAction 실패 시 에러를 전파한다', async () => {
    authApi.login.mockRejectedValue(new Error('이메일 또는 비밀번호가 올바르지 않습니다.'))

    const { result } = renderHook(() => useAuthStore())
    await expect(
      act(async () => { await result.current.loginAction({ email: 'bad@email.com', password: 'wrong' }) })
    ).rejects.toThrow('이메일 또는 비밀번호가 올바르지 않습니다.')

    expect(result.current.isAuthenticated).toBe(false)
  })
})