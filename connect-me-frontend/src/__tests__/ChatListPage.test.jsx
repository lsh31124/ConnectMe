import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import ChatListPage from '../pages/ChatListPage'
import * as chatApi from '../api/chatApi'
import useAuthStore from '../store/useAuthStore'

vi.mock('../api/chatApi')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

const mockChatRooms = [
  {
    id: 1,
    name: 'Sarah Wilson',
    lastMessage: 'Typing...',
    lastMessageTime: '12:45 PM',
    unreadCount: 3,
    profileImage: null,
  },
  {
    id: 2,
    name: 'Design Team',
    lastMessage: 'The latest mocks look great!...',
    lastMessageTime: '9:12 AM',
    unreadCount: 0,
    profileImage: null,
  },
  {
    id: 3,
    name: 'Alex Johnson',
    lastMessage: 'Hey, are we still meeting for...',
    lastMessageTime: 'Yesterday',
    unreadCount: 1,
    profileImage: null,
  },
]

beforeEach(() => {
  useAuthStore.setState({ accessToken: null, refreshToken: null, isAuthenticated: false })
  vi.resetAllMocks()
  mockNavigate.mockClear()
})

describe('ChatListPage', () => {
  it('헤더 타이틀과 검색 입력을 렌더링한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: [] })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    expect(screen.getByText('Messages')).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/Search conversations/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.queryByText(/로딩 중/i)).not.toBeInTheDocument()
    })
  })

  it('로딩 중에는 로딩 표시를 보여준다', () => {
    chatApi.getChatRooms.mockReturnValue(new Promise(() => {}))

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    expect(screen.getByText(/로딩 중/i)).toBeInTheDocument()
  })

  it('데이터 로드 후 채팅방 목록을 렌더링한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: mockChatRooms })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('Sarah Wilson')).toBeInTheDocument()
      expect(screen.getByText('Design Team')).toBeInTheDocument()
      expect(screen.getByText('Alex Johnson')).toBeInTheDocument()
    })
  })

  it('마지막 메시지 미리보기와 시간을 표시한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: mockChatRooms })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('Typing...')).toBeInTheDocument()
      expect(screen.getByText('12:45 PM')).toBeInTheDocument()
    })
  })

  it('읽지 않은 메시지 뱃지를 표시한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: mockChatRooms })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('3')).toBeInTheDocument()
      expect(screen.getByText('1')).toBeInTheDocument()
    })
  })

  it('빈 목록일 때 안내 메시지를 표시한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: [] })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText(/아직 채팅방이 없습니다/i)).toBeInTheDocument()
    })
  })

  it('API 오류 시 에러 메시지를 표시한다', async () => {
    chatApi.getChatRooms.mockRejectedValue(new Error('서버 오류가 발생했습니다.'))

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText(/서버 오류가 발생했습니다/i)).toBeInTheDocument()
    })
  })

  it('검색어 입력 시 목록을 필터링한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: mockChatRooms })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('Sarah Wilson')).toBeInTheDocument()
    })

    fireEvent.change(screen.getByPlaceholderText(/Search conversations/i), {
      target: { value: 'Design' },
    })

    await waitFor(() => {
      expect(screen.getByText('Design Team')).toBeInTheDocument()
      expect(screen.queryByText('Sarah Wilson')).not.toBeInTheDocument()
      expect(screen.queryByText('Alex Johnson')).not.toBeInTheDocument()
    })
  })

  it('하단 탭바에 CHATS, FRIENDS, HOME, MORE를 렌더링한다', async () => {
    chatApi.getChatRooms.mockResolvedValue({ data: [] })

    render(<MemoryRouter><ChatListPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('CHATS')).toBeInTheDocument()
      expect(screen.getByText('FRIENDS')).toBeInTheDocument()
      expect(screen.getByText('HOME')).toBeInTheDocument()
      expect(screen.getByText('MORE')).toBeInTheDocument()
    })
  })
})