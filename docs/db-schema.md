# DB Schema — SNS Chat Service

## ERD

```
┌──────────────────────────────────────────────────────────────────────────┐
│  users                                                                   │
│  ────────────────────────────────────────────────────────────────────    │
│  id              BIGINT PK AUTO_INCREMENT                                │
│  phone_number    VARCHAR(20) UNIQUE                                      │
│  email           VARCHAR(255) UNIQUE                                     │
│  password_hash   VARCHAR(255)                                            │
│  name            VARCHAR(100) NOT NULL                                   │
│  profile_image   VARCHAR(500)                                            │
│  status_message  VARCHAR(500)                                            │
│  provider        ENUM('LOCAL','GOOGLE','APPLE') NOT NULL DEFAULT 'LOCAL' │
│  provider_id     VARCHAR(255)                                            │
│  deleted_at      DATETIME NULL   ← soft delete                          │
│  created_at      DATETIME NOT NULL                                       │
│  updated_at      DATETIME NOT NULL                                       │
└──────────────────┬───────────────────────────────────────────────────────┘
                   │
     ┌─────────────┼──────────────────┬──────────────────┐
     │             │                  │                  │
     ▼             ▼                  ▼                  ▼
┌──────────────┐ ┌────────────────┐ ┌──────────────────┐ ┌────────────────────┐
│  friends     │ │  chat_rooms    │ │  notifications   │ │  refresh_tokens    │
│  ────────────│ │  ──────────────│ │  ────────────────│ │  ──────────────────│
│  id    BIGINT│ │  id      BIGINT│ │  id      BIGINT  │ │  id      BIGINT PK │
│  requester_id│ │  name  VARCHAR │ │  user_id BIGINT  │ │  user_id BIGINT FK │
│       BIGINT │ │  type    ENUM  │ │  type    ENUM    │ │  token   VARCHAR   │
│  receiver_id │ │  ('DIRECT',   │ │  ('MESSAGE',     │ │          (500)     │
│       BIGINT │ │   'GROUP')    │ │   'FRIEND_       │ │  expiry_date       │
│  status ENUM │ │  created_by   │ │   REQUEST')      │ │      DATETIME      │
│  ('PENDING', │ │      BIGINT   │ │  message_id      │ │  UNIQUE(token)     │
│  'ACCEPTED', │ │  pinned_msg_id│ │      BIGINT NULL │ │                    │
│  'BLOCKED')  │ │      BIGINT   │ │  friend_id       │ └────────────────────┘
│  blocked_by_id  │ direct_room_  │ │      BIGINT NULL │
│       BIGINT │ │  key VARCHAR  │ │  is_read BOOLEAN │
│  UNIQUE(     │ │  (30) UNIQUE  │ │  created_at      │
│  requester_id│ │  created_at   │ │      DATETIME    │
│  ,receiver_id│ │  updated_at   │ └──────────────────┘
│  )           │ └──────┬────────┘
└──────────────┘        │
              ┌─────────┴──────────┐
              │                   │
              ▼                   ▼
┌─────────────────────────┐  ┌──────────────────────────────────┐
│  chat_room_members      │  │  messages                        │
│  ───────────────────────│  │  ────────────────────────────────│
│  id            BIGINT PK│  │  id              BIGINT PK       │
│  chat_room_id  BIGINT FK│  │  chat_room_id    BIGINT FK       │
│  user_id       BIGINT FK│  │  sender_id       BIGINT FK       │
│  role  ENUM             │  │  type  ENUM                      │
│  ('OWNER','MEMBER')     │  │  ('TEXT','IMAGE','FILE')         │
│  is_pinned     BOOLEAN  │  │  content         VARCHAR(4000)   │
│  last_read_    BIGINT   │  │  file_url        VARCHAR(1000)   │
│    message_id  NULL     │  │  file_name       VARCHAR(255)    │
│  joined_at     DATETIME │  │  file_size       BIGINT          │
│  left_at       DATETIME │  │  is_deleted      BOOLEAN         │
│                         │  │  created_at      DATETIME        │
│  활성 멤버 중복 방지:     │  └──────────────────────────────────┘
│  UNIQUE(chat_room_id,   │
│   user_id) WHERE        │
│   left_at IS NULL       │
│  (DB partial index)     │
└─────────────────────────┘
```

## 테이블 설명

| 테이블 | 설명 |
|---|---|
| `users` | 회원 정보. 로컬/소셜(Google, Apple) 로그인 통합. `deleted_at`으로 soft delete |
| `friends` | 친구 관계. `requester_id`/`receiver_id` 방향성 있음. `blocked_by_id`로 차단자 식별 |
| `chat_rooms` | 채팅방. DIRECT(1:1) / GROUP 구분. `direct_room_key`로 DIRECT 중복 방지 |
| `chat_room_members` | 채팅방 멤버. `last_read_message_id`로 읽음 상태 추적 |
| `messages` | 메시지. TEXT / IMAGE / FILE 타입 지원. `is_deleted`로 soft delete |
| `notifications` | 알림. `message_id` / `friend_id` FK로 이벤트 소스 명확화 |
| `refresh_tokens` | JWT Refresh Token 저장소. 토큰 문자열 UNIQUE 제약 |

## 주요 제약

- `friends.(requester_id, receiver_id)`: UNIQUE (중복 친구 관계 방지)
- `friends.blocked_by_id`: 차단자 ID — `requester_id` 또는 `receiver_id` 중 하나
- `chat_rooms.direct_room_key`: `min(userId)_max(userId)` 형식, UNIQUE (DIRECT 채팅방 중복 방지)
- `chat_room_members.last_read_message_id`: 안읽음 수 = 최신 message.id - last_read_message_id
- `chat_room_members` 활성 멤버 UNIQUE: `(chat_room_id, user_id) WHERE left_at IS NULL` partial index
- `messages.is_deleted`: soft delete (내용은 보존, 클라이언트에 삭제 표시)
- `messages.content`: VARCHAR(4000), `file_url`: VARCHAR(1000)
- `users.provider_id`: 소셜 로그인 식별자 (Google sub, Apple sub)
- `users.deleted_at`: soft delete — `@SQLRestriction("deleted_at IS NULL")`으로 조회 시 자동 필터링