import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import ChatRoomPage from '../pages/ChatRoomPage'
import { getChatRoomDetail } from '../api/chatApi'
import { getMessages } from '../api/messageApi'

const mockNavigate = vi.fn()
const mockPublish = vi.fn()
const mockActivate = vi.fn()
const mockDeactivate = vi.fn()
let capturedOnConnect = null

vi.mock('@stomp/stompjs', () => {
  const ClientMock = vi.fn().mockImplementation(({ onConnect }) => {
    capturedOnConnect = onConnect
    return {
      activate: mockActivate,
      deactivate: mockDeactivate,
      publish: mockPublish,
    }
  })
  return { Client: ClientMock }
})

vi.mock('../api/chatApi', () => ({ getChatRoomDetail: vi.fn() }))
vi.mock('../api/messageApi', () => ({ getMessages: vi.fn() }))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate, useParams: () => ({ roomId: '1' }) }
})

vi.mock('../store/useAuthStore', () => {
  const mockStore = (selector) => selector({ accessToken: 'mock.token.value', isAuthenticated: true })
  mockStore.getState = () => ({ accessToken: 'mock.token.value', isAuthenticated: true })
  mockStore.setState = vi.fn()
  mockStore.subscribe = vi.fn()
  return { default: mockStore }
})

beforeEach(() => {
  mockNavigate.mockClear()
  mockPublish.mockClear()
  mockActivate.mockClear()
  mockDeactivate.mockClear()
  capturedOnConnect = null
  getChatRoomDetail.mockReset()
  getMessages.mockReset()
  getChatRoomDetail.mockResolvedValue({ data: { id: 1, name: '테스트 채팅방', type: 'DIRECT' } })
  getMessages.mockResolvedValue({ data: { messages: [], nextCursor: null } })
})

describe('ChatRoomPage', () => {
  it('헤더와 입력창을 렌더링한다', async () => {
    render(<MemoryRouter><ChatRoomPage /></MemoryRouter>)

    expect(screen.getByPlaceholderText('Type a message...')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '전송' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '뒤로' })).toBeInTheDocument()
  })

  it('채팅방 정보 로드 후 방 이름이 표시된다', async () => {
    render(<MemoryRouter><ChatRoomPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('테스트 채팅방')).toBeInTheDocument()
    })
  })

  it('메시지 히스토리를 로드하여 표시한다', async () => {
    getMessages.mockResolvedValue({
      data: {
        messages: [
          { id: 1, senderId: 99, content: '안녕하세요!', createdAt: '2024-01-01T10:30:00', deleted: false },
          { id: 2, senderId: 100, content: '반갑습니다!', createdAt: '2024-01-01T10:31:00', deleted: false },
        ],
        nextCursor: null,
      },
    })

    render(<MemoryRouter><ChatRoomPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByText('안녕하세요!')).toBeInTheDocument()
      expect(screen.getByText('반갑습니다!')).toBeInTheDocument()
    })
  })

  it('빈 메시지 전송 시 publish를 호출하지 않는다', async () => {
    render(<MemoryRouter><ChatRoomPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Type a message...')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: '전송' }))

    expect(mockPublish).not.toHaveBeenCalled()
  })

  it('메시지 입력 후 전송 버튼 클릭 시 입력창이 초기화된다', async () => {
    render(<MemoryRouter><ChatRoomPage /></MemoryRouter>)

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Type a message...')).toBeInTheDocument()
    })

    const input = screen.getByPlaceholderText('Type a message...')
    fireEvent.change(input, { target: { value: '테스트 메시지' } })
    expect(input.value).toBe('테스트 메시지')

    fireEvent.click(screen.getByRole('button', { name: '전송' }))

    await waitFor(() => {
      expect(input.value).toBe('')
    })
  })
})