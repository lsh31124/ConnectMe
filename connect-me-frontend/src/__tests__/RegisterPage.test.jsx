import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import RegisterPage from '../pages/RegisterPage'
import * as authApi from '../api/authApi'
import useAuthStore from '../store/useAuthStore'

vi.mock('../api/authApi')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

beforeEach(() => {
  localStorage.clear()
  useAuthStore.setState({ accessToken: null, refreshToken: null, isAuthenticated: false })
  vi.resetAllMocks()
  mockNavigate.mockClear()
})

describe('RegisterPage', () => {
  it('이름, 이메일, 비밀번호 필드와 제출 버튼을 렌더링한다', () => {
    render(<MemoryRouter><RegisterPage /></MemoryRouter>)

    expect(screen.getByPlaceholderText(/이름/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/이메일/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/비밀번호/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument()
  })

  it('빈 폼 제출 시 유효성 오류를 표시한다', async () => {
    render(<MemoryRouter><RegisterPage /></MemoryRouter>)

    fireEvent.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(screen.getByText(/모든 항목을 입력해주세요/i)).toBeInTheDocument()
    })
  })

  it('비밀번호 8자 미만 시 유효성 오류를 표시한다', async () => {
    render(<MemoryRouter><RegisterPage /></MemoryRouter>)

    fireEvent.change(screen.getByPlaceholderText(/이름/i), { target: { value: '홍길동' } })
    fireEvent.change(screen.getByPlaceholderText(/이메일/i), { target: { value: 'test@email.com' } })
    fireEvent.change(screen.getByPlaceholderText(/비밀번호/i), { target: { value: '1234567' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(screen.getByText(/비밀번호는 8자 이상/i)).toBeInTheDocument()
    })
  })

  it('회원가입 성공 시 /home으로 이동한다', async () => {
    authApi.register.mockResolvedValue({ data: { accessToken: 'access', refreshToken: 'refresh' } })

    render(<MemoryRouter><RegisterPage /></MemoryRouter>)

    fireEvent.change(screen.getByPlaceholderText(/이름/i), { target: { value: '홍길동' } })
    fireEvent.change(screen.getByPlaceholderText(/이메일/i), { target: { value: 'test@email.com' } })
    fireEvent.change(screen.getByPlaceholderText(/비밀번호/i), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/home')
    })
  })

  it('서버 오류 시 에러 메시지를 표시한다', async () => {
    authApi.register.mockRejectedValue(new Error('이미 사용 중인 이메일입니다.'))

    render(<MemoryRouter><RegisterPage /></MemoryRouter>)

    fireEvent.change(screen.getByPlaceholderText(/이름/i), { target: { value: '홍길동' } })
    fireEvent.change(screen.getByPlaceholderText(/이메일/i), { target: { value: 'dup@email.com' } })
    fireEvent.change(screen.getByPlaceholderText(/비밀번호/i), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(screen.getByText(/이미 사용 중인 이메일입니다/i)).toBeInTheDocument()
    })
  })
})