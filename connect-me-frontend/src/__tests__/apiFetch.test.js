import { describe, it, expect, vi, beforeEach } from 'vitest'
import { apiFetch } from '../api/apiFetch'
import * as authApi from '../api/authApi'
import useAuthStore from '../store/useAuthStore'

vi.mock('../api/authApi')

beforeEach(() => {
  vi.resetAllMocks()
  useAuthStore.setState({
    accessToken: 'old-access',
    refreshToken: 'old-refresh',
    isAuthenticated: true,
  })
})

describe('apiFetch', () => {
  it('성공 응답을 그대로 반환한다', async () => {
    const mockRes = { ok: true, status: 200, json: () => Promise.resolve({ data: 'ok' }) }
    global.fetch = vi.fn().mockResolvedValue(mockRes)

    const res = await apiFetch('/users/me')

    expect(res).toBe(mockRes)
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  it('accessToken이 있으면 Authorization 헤더를 포함해서 요청한다', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, status: 200 })

    await apiFetch('/users/me')

    expect(fetch).toHaveBeenCalledWith(
      '/users/me',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer old-access',
        }),
      })
    )
  })

  it('403 응답 시 refreshTokens를 호출하고 새 토큰으로 재요청한다', async () => {
    const mockRefreshData = {
      data: { accessToken: 'new-access', refreshToken: 'new-refresh' },
    }
    authApi.refreshTokens.mockResolvedValue(mockRefreshData)

    const mockSuccessRes = { ok: true, status: 200 }

    global.fetch = vi.fn()
      .mockResolvedValueOnce({ ok: false, status: 403 })
      .mockResolvedValueOnce(mockSuccessRes)

    const res = await apiFetch('/users/me')

    expect(authApi.refreshTokens).toHaveBeenCalledWith('old-refresh')
    expect(fetch).toHaveBeenCalledTimes(2)
    expect(res).toBe(mockSuccessRes)
  })

  it('갱신 성공 시 store에 새 토큰을 저장한다', async () => {
    const mockRefreshData = {
      data: { accessToken: 'new-access', refreshToken: 'new-refresh' },
    }
    authApi.refreshTokens.mockResolvedValue(mockRefreshData)

    global.fetch = vi.fn()
      .mockResolvedValueOnce({ ok: false, status: 403 })
      .mockResolvedValueOnce({ ok: true, status: 200 })

    await apiFetch('/users/me')

    expect(useAuthStore.getState().accessToken).toBe('new-access')
    expect(useAuthStore.getState().refreshToken).toBe('new-refresh')
  })

  it('갱신 실패 시 로그아웃하고 에러를 던진다', async () => {
    authApi.refreshTokens.mockRejectedValue(new Error('갱신 실패'))
    authApi.logout.mockResolvedValue(undefined)

    global.fetch = vi.fn().mockResolvedValue({ ok: false, status: 403 })

    await expect(apiFetch('/users/me')).rejects.toThrow('세션이 만료되었습니다. 다시 로그인해 주세요.')

    expect(useAuthStore.getState().isAuthenticated).toBe(false)
  })
})