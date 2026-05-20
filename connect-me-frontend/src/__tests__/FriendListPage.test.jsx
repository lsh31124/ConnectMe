import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import FriendListPage from '../pages/FriendListPage'
import * as friendApi from '../api/friendApi'
import useAuthStore from '../store/useAuthStore'

vi.mock('../api/friendApi')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

const mockFriends = [
  { id: 1, friendId: 2, name: '김철수', email: 'kim@example.com' },
  { id: 2, friendId: 3, name: '이영희', email: 'lee@example.com' },
]

const mockRequests = [
  { id: 3, requesterId: 4, name: '박지수', email: 'park@example.com' },
]

beforeEach(() => {
  localStorage.clear()
  useAuthStore.setState({ accessToken: 'test-token', refreshToken: 'refresh-token', isAuthenticated: true })
  vi.resetAllMocks()
  mockNavigate.mockClear()
})

describe('FriendListPage', () => {
  it('친구 목록 탭과 친구 요청 탭을 렌더링한다', async () => {
    friendApi.getFriends.mockResolvedValue({ data: [] })
    friendApi.getFriendRequests.mockResolvedValue({ data: [] })

    render(<MemoryRouter><FriendListPage /></MemoryRouter>)

    expect(screen.getByRole('button', { name: /친구 목록/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /친구 요청/i })).toBeInTheDocument()
  })

  it('친구 목록을 로드해서 렌더링한다', async () => {
    friendApi.getFriends.mockResolvedValue({ data: mockFriends })
    friendApi.getFriendRequests.mockResolvedValue({ data: [] })

    render(<MemoryRouter><FriendListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('김철수')).toBeInTheDocument()
      expect(screen.getByText('이영희')).toBeInTheDocument()
    })
  })

  it('친구 목록이 비어 있을 때 안내 메시지를 표시한다', async () => {
    friendApi.getFriends.mockResolvedValue({ data: [] })
    friendApi.getFriendRequests.mockResolvedValue({ data: [] })

    render(<MemoryRouter><FriendListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText(/친구가 없습니다/i)).toBeInTheDocument()
    })
  })

  it('친구 요청 탭 클릭 시 요청 목록을 보여준다', async () => {
    friendApi.getFriends.mockResolvedValue({ data: [] })
    friendApi.getFriendRequests.mockResolvedValue({ data: mockRequests })

    render(<MemoryRouter><FriendListPage /></MemoryRouter>)

    await waitFor(() => expect(friendApi.getFriendRequests).toHaveBeenCalled())

    fireEvent.click(screen.getByRole('button', { name: /친구 요청/i }))

    await waitFor(() => {
      expect(screen.getByText('박지수')).toBeInTheDocument()
    })
  })

  it('수락 버튼 클릭 시 acceptFriend를 호출한다', async () => {
    friendApi.getFriends.mockResolvedValue({ data: [] })
    friendApi.getFriendRequests.mockResolvedValue({ data: mockRequests })
    friendApi.acceptFriend.mockResolvedValue({ data: {} })

    render(<MemoryRouter><FriendListPage /></MemoryRouter>)

    fireEvent.click(screen.getByRole('button', { name: /친구 요청/i }))

    await waitFor(() => expect(screen.getByText('박지수')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: /수락/i }))

    await waitFor(() => {
      expect(friendApi.acceptFriend).toHaveBeenCalledWith(3, 'test-token')
    })
  })

  it('거절 버튼 클릭 시 rejectFriend를 호출한다', async () => {
    friendApi.getFriends.mockResolvedValue({ data: [] })
    friendApi.getFriendRequests.mockResolvedValue({ data: mockRequests })
    friendApi.rejectFriend.mockResolvedValue(undefined)

    render(<MemoryRouter><FriendListPage /></MemoryRouter>)

    fireEvent.click(screen.getByRole('button', { name: /친구 요청/i }))

    await waitFor(() => expect(screen.getByText('박지수')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: /거절/i }))

    await waitFor(() => {
      expect(friendApi.rejectFriend).toHaveBeenCalledWith(3, 'test-token')
    })
  })
})