import { describe, it, expect, vi, beforeEach } from 'vitest'
import { login, register, logout } from '../api/authApi'

beforeEach(() => {
  vi.resetAllMocks()
})

describe('authApi.login', () => {
  it('성공 시 응답 데이터를 반환한다', async () => {
    const mockData = { code: 'SUCCESS', data: { accessToken: 'access', refreshToken: 'refresh', tokenType: 'Bearer' } }
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve(mockData) })

    const result = await login({ email: 'test@email.com', password: 'password123' })

    expect(fetch).toHaveBeenCalledWith('/auth/login', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ email: 'test@email.com', password: 'password123' }),
    }))
    expect(result).toEqual(mockData)
  })

  it('실패 시 서버 메시지로 에러를 던진다', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.resolve({ message: '이메일 또는 비밀번호가 올바르지 않습니다.' }),
    })

    await expect(login({ email: 'bad@email.com', password: 'wrong' }))
      .rejects.toThrow('이메일 또는 비밀번호가 올바르지 않습니다.')
  })
})

describe('authApi.register', () => {
  it('성공 시 응답 데이터를 반환한다', async () => {
    const mockData = { code: 'SUCCESS', data: { accessToken: 'access', refreshToken: 'refresh', tokenType: 'Bearer' } }
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve(mockData) })

    const result = await register({ email: 'new@email.com', password: 'password123', name: '홍길동' })

    expect(result).toEqual(mockData)
  })

  it('중복 이메일 시 에러를 던진다', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.resolve({ message: '이미 사용 중인 이메일입니다.' }),
    })

    await expect(register({ email: 'dup@email.com', password: 'password123', name: '홍길동' }))
      .rejects.toThrow('이미 사용 중인 이메일입니다.')
  })
})

describe('authApi.logout', () => {
  it('로그아웃 엔드포인트를 호출한다', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true })

    await logout('some-refresh-token')

    expect(fetch).toHaveBeenCalledWith('/auth/logout', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ refreshToken: 'some-refresh-token' }),
    }))
  })
})

import { refreshTokens } from '../api/authApi'

describe('authApi.refreshTokens', () => {
  it('POST /auth/refresh를 호출하고 새 토큰을 반환한다', async () => {
    const mockData = {
      code: 'SUCCESS',
      data: { accessToken: 'new-access', refreshToken: 'new-refresh' },
    }
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve(mockData) })

    const result = await refreshTokens('old-refresh')

    expect(fetch).toHaveBeenCalledWith('/auth/refresh', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ refreshToken: 'old-refresh' }),
    }))
    expect(result).toEqual(mockData)
  })

  it('갱신 실패 시 에러를 던진다', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.resolve({ message: '토큰이 유효하지 않습니다.' }),
    })

    await expect(refreshTokens('invalid-token')).rejects.toThrow('토큰이 유효하지 않습니다.')
  })
})