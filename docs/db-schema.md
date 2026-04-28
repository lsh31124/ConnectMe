# DB Schema — SNS Chat Service

## ERD

```
┌─────────────────────────────────────────────────────────────────────┐
│  users                                                              │
│  ─────────────────────────────────────────────────────────────────  │
│  id            BIGINT PK AUTO_INCREMENT                             │
│  phone_number  VARCHAR(20) UNIQUE                                   │
│  email         VARCHAR(255) UNIQUE                                  │
│  password_hash VARCHAR(255)                                         │
│  name          VARCHAR(100) NOT NULL                                │
│  profile_image VARCHAR(500)                                         │
│  status_message VARCHAR(500)                                        │
│  provider      ENUM('LOCAL','GOOGLE','APPLE') NOT NULL DEFAULT 'LOCAL'│
│  provider_id   VARCHAR(255)                                         │
│  created_at    DATETIME NOT NULL                                    │
│  updated_at    DATETIME NOT NULL                                    │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────────┐
         │                 │                     │
         ▼                 ▼                     ▼
┌────────────────┐ ┌───────────────┐  ┌──────────────────────┐
│  friends       │ │  chat_rooms   │  │  notifications       │
│  ──────────────│ │  ─────────────│  │  ────────────────────│
│  id      BIGINT│ │  id     BIGINT│  │  id        BIGINT PK │
│  user_id BIGINT│ │  name VARCHAR │  │  user_id   BIGINT FK │
│  friend_id     │ │  type   ENUM  │  │  type      ENUM      │
│        BIGINT  │ │  ('DIRECT',   │  │  ('MESSAGE',         │
│  status  ENUM  │ │   'GROUP')    │  │   'FRIEND_REQUEST')  │
│  ('PENDING',   │ │  created_by   │  │  ref_id    BIGINT    │
│   'ACCEPTED',  │ │       BIGINT  │  │  is_read   BOOLEAN   │
│   'BLOCKED')   │ │  pinned_msg_id│  │  created_at DATETIME │
│  created_at    │ │       BIGINT  │  └──────────────────────┘
│      DATETIME  │ │  created_at   │
│  UNIQUE(user_id│ │      DATETIME │
│   ,friend_id)  │ │  updated_at   │
└────────────────┘ │      DATETIME │
                   └───────┬───────┘
                           │
            ┌──────────────┴──────────────┐
            │                             │
            ▼                             ▼
┌───────────────────────┐   ┌──────────────────────────────┐
│  chat_room_members    │   │  messages                    │
│  ─────────────────────│   │  ────────────────────────────│
│  id          BIGINT PK│   │  id            BIGINT PK     │
│  chat_room_id BIGINT FK│  │  chat_room_id  BIGINT FK     │
│  user_id     BIGINT FK│   │  sender_id     BIGINT FK     │
│  role    ENUM         │   │  type  ENUM                  │
│  ('OWNER','MEMBER')   │   │  ('TEXT','IMAGE','FILE')     │
│  is_pinned   BOOLEAN  │   │  content       TEXT          │
│  joined_at   DATETIME │   │  file_url      VARCHAR(500)  │
│  left_at     DATETIME │   │  file_name     VARCHAR(255)  │
└───────────────────────┘   │  file_size     BIGINT        │
                            │  is_deleted    BOOLEAN       │
                            │  created_at    DATETIME      │
                            └──────────────────────────────┘
```

## 테이블 설명

| 테이블 | 설명 |
|---|---|
| `users` | 회원 정보. 로컬/소셜(Google, Apple) 로그인 통합 |
| `friends` | 친구 관계. 양방향 참조, 상태(pending/accepted/blocked) 관리 |
| `chat_rooms` | 채팅방. DIRECT(1:1) / GROUP 구분 |
| `chat_room_members` | 채팅방 멤버. 핀 고정, 권한(OWNER/MEMBER) 포함 |
| `messages` | 메시지. TEXT / IMAGE / FILE 타입 지원 |
| `notifications` | 알림. 메시지·친구 요청 등 다양한 이벤트 수신 |

## 주요 제약

- `friends.user_id + friend_id`: UNIQUE (중복 친구 관계 방지)
- `messages.is_deleted`: soft delete (내용은 보존, 클라이언트에 삭제 표시)
- `chat_rooms.pinned_msg_id`: 그룹 채팅 핀 고정 메시지
- `users.provider_id`: 소셜 로그인 식별자 (Google sub, Apple sub)