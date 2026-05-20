import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import ProfilePage from '../pages/ProfilePage'
import * as userApi from '../api/userApi'
import useAuthStore from '../store/useAuthStore'

vi.mock('../api/userApi')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

const mockProfile = {
  name: '홍길동',
  email: 'hong@example.com',
  statusMessage: '안녕하세요',
  profileImage: null,
}

beforeEach(() => {
  localStorage.clear()
  useAuthStore.setState({ accessToken: 'test-token', refreshToken: 'refresh-token', isAuthenticated: true })
  vi.resetAllMocks()
  mockNavigate.mockClear()
})

describe('ProfilePage', () => {
  it('프로필 정보를 로드해서 렌더링한다', async () => {
    userApi.getMyProfile.mockResolvedValue({ data: mockProfile })

    render(<MemoryRouter><ProfilePage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('홍길동')).toBeInTheDocument()
      expect(screen.getByText('hong@example.com')).toBeInTheDocument()
      expect(screen.getByText('안녕하세요')).toBeInTheDocument()
    })
  })

  it('편집 버튼 클릭 시 수정 모드로 전환된다', async () => {
    userApi.getMyProfile.mockResolvedValue({ data: mockProfile })

    render(<MemoryRouter><ProfilePage /></MemoryRouter>)

    await waitFor(() => expect(screen.getByText('홍길동')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: /편집/i }))

    expect(screen.getByDisplayValue('홍길동')).toBeInTheDocument()
    expect(screen.getByDisplayValue('안녕하세요')).toBeInTheDocument()
  })

  it('저장 성공 시 성공 메시지를 표시한다', async () => {
    userApi.getMyProfile.mockResolvedValue({ data: mockProfile })
    userApi.updateMyProfile.mockResolvedValue({ data: { ...mockProfile, name: '새이름' } })

    render(<MemoryRouter><ProfilePage /></MemoryRouter>)

    await waitFor(() => expect(screen.getByText('홍길동')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: /편집/i }))

    fireEvent.change(screen.getByDisplayValue('홍길동'), { target: { value: '새이름' } })
    fireEvent.click(screen.getByRole('button', { name: /저장/i }))

    await waitFor(() => {
      expect(screen.getByText(/저장되었습니다/i)).toBeInTheDocument()
    })
  })

  it('저장 실패 시 에러 메시지를 표시한다', async () => {
    userApi.getMyProfile.mockResolvedValue({ data: mockProfile })
    userApi.updateMyProfile.mockRejectedValue(new Error('서버 오류가 발생했습니다.'))

    render(<MemoryRouter><ProfilePage /></MemoryRouter>)

    await waitFor(() => expect(screen.getByText('홍길동')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: /편집/i }))
    fireEvent.click(screen.getByRole('button', { name: /저장/i }))

    await waitFor(() => {
      expect(screen.getByText(/서버 오류가 발생했습니다/i)).toBeInTheDocument()
    })
  })

  it('취소 클릭 시 뷰 모드로 돌아간다', async () => {
    userApi.getMyProfile.mockResolvedValue({ data: mockProfile })

    render(<MemoryRouter><ProfilePage /></MemoryRouter>)

    await waitFor(() => expect(screen.getByText('홍길동')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: /편집/i }))
    expect(screen.getByDisplayValue('홍길동')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /취소/i }))

    expect(screen.queryByDisplayValue('홍길동')).not.toBeInTheDocument()
    expect(screen.getByText('홍길동')).toBeInTheDocument()
  })
})