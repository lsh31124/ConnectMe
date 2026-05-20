import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import PrivateRoute from '../components/PrivateRoute'
import useAuthStore from '../store/useAuthStore'

beforeEach(() => {
  localStorage.clear()
  useAuthStore.setState({ accessToken: null, refreshToken: null, isAuthenticated: false })
})

describe('PrivateRoute', () => {
  it('비인증 상태에서 /login으로 리다이렉트한다', () => {
    useAuthStore.setState({ isAuthenticated: false }, true)

    render(
      <MemoryRouter initialEntries={['/home']}>
        <Routes>
          <Route path="/login" element={<div>로그인 페이지</div>} />
          <Route element={<PrivateRoute />}>
            <Route path="/home" element={<div>홈 페이지</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    )

    expect(screen.getByText('로그인 페이지')).toBeInTheDocument()
    expect(screen.queryByText('홈 페이지')).not.toBeInTheDocument()
  })

  it('인증 상태에서 보호된 콘텐츠를 렌더링한다', () => {
    useAuthStore.setState({ isAuthenticated: true }, true)

    render(
      <MemoryRouter initialEntries={['/home']}>
        <Routes>
          <Route path="/login" element={<div>로그인 페이지</div>} />
          <Route element={<PrivateRoute />}>
            <Route path="/home" element={<div>홈 페이지</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    )

    expect(screen.getByText('홈 페이지')).toBeInTheDocument()
    expect(screen.queryByText('로그인 페이지')).not.toBeInTheDocument()
  })
})