# ConnectMe Architecture

## 1. 멀티 모듈 의존성

```mermaid
graph LR
    API["📦 connect-me-api\n(Spring Boot App)"]
    DOMAIN["📦 connect-me-domain\n(Entity · Repository)"]
    COMMON["📦 connect-me-common\n(Exception · Util)"]

    API --> DOMAIN
    API --> COMMON
    DOMAIN --> COMMON
```

---

## 2. 레이어 아키텍처

```mermaid
graph TB
    subgraph CLIENT["Client"]
        HTTP["HTTP Request"]
    end

    subgraph API["connect-me-api"]
        subgraph SECURITY["Security Layer"]
            JWF["JwtAuthenticationFilter"]
            JWT["JwtTokenProvider"]
            UDS["CustomUserDetailsService"]
            SC["SecurityConfig"]
        end

        subgraph CTRL["Controller Layer"]
            AC["AuthController\n/auth/**"]
            UC["UserController\n/users/**"]
            FC["FriendController\n/friends/**"]
            CC["ChatRoomController\n/chat-rooms/**"]
        end

        subgraph SVC["Service Layer"]
            AS["AuthService"]
            US["UserService"]
            FS["FriendService"]
            CS["ChatRoomService"]
        end

        subgraph GLOBAL["Global"]
            GEH["GlobalExceptionHandler"]
            AR["ApiResponse&lt;T&gt;"]
        end
    end

    subgraph DOMAIN["connect-me-domain"]
        UE["User"]
        RT["RefreshToken"]
        UR["UserRepository"]
        RTR["RefreshTokenRepository"]
        FE["Friend"]
        FR["FriendRepository"]
        CRE["ChatRoom"]
        CRM["ChatRoomMember"]
        CRR["ChatRoomRepository"]
        CRMR["ChatRoomMemberRepository"]
        BTE["BaseTimeEntity"]
    end

    subgraph COMMON["connect-me-common"]
        EC["ErrorCode"]
        BE["BusinessException"]
    end

    HTTP --> JWF
    JWF --> JWT
    JWF --> UDS
    JWF --> CTRL
    AC --> AS
    UC --> US
    FC --> FS
    CC --> CS
    AS --> UR
    AS --> RTR
    US --> UR
    FS --> FR
    CS --> CRR
    CS --> CRMR
    AS --> BE
    US --> BE
    FS --> BE
    CS --> BE
    BE --> EC
    UE --> BTE
    FE --> BTE
    CRE --> BTE
    CRM --> BTE
    UR --> UE
    RTR --> RT
    FR --> FE
    CRR --> CRE
    CRMR --> CRM
```

---

## 3. 도메인 엔티티 관계

```mermaid
erDiagram
    users {
        bigint id PK
        varchar email UK
        varchar phone_number UK
        varchar password_hash
        varchar name
        varchar profile_image
        varchar status_message
        enum provider "LOCAL·GOOGLE·APPLE"
        varchar provider_id
        datetime created_at
        datetime updated_at
    }

    refresh_tokens {
        bigint id PK
        varchar token UK
        bigint user_id FK
        datetime expiry_date
    }

    friends {
        bigint id PK
        bigint requester_id FK
        bigint receiver_id FK
        enum status "PENDING·ACCEPTED·BLOCKED"
        datetime created_at
        datetime updated_at
    }

    chat_rooms {
        bigint id PK
        varchar name
        enum type "DIRECT·GROUP"
        bigint created_by FK
        bigint pinned_msg_id FK
        datetime created_at
        datetime updated_at
    }

    chat_room_members {
        bigint id PK
        bigint chat_room_id FK
        bigint user_id FK
        enum role "OWNER·MEMBER"
        boolean is_pinned
        datetime joined_at
        datetime left_at
    }

    messages {
        bigint id PK
        bigint chat_room_id FK
        bigint sender_id FK
        enum type "TEXT·IMAGE·FILE"
        text content
        varchar file_url
        varchar file_name
        bigint file_size
        boolean is_deleted
        datetime created_at
    }

    notifications {
        bigint id PK
        bigint user_id FK
        enum type "MESSAGE·FRIEND_REQUEST"
        bigint ref_id
        boolean is_read
        datetime created_at
    }

    users ||--o{ refresh_tokens    : "발급"
    users ||--o{ friends           : "requester_id"
    users ||--o{ friends           : "receiver_id"
    users ||--o{ chat_room_members : "참여"
    users ||--o{ messages          : "전송"
    users ||--o{ notifications     : "수신"
    chat_rooms ||--o{ chat_room_members : "포함"
    chat_rooms ||--o{ messages          : "포함"
```

---

## 4. API 요청 흐름

### 4-1. 인증 흐름

```mermaid
sequenceDiagram
    actor C as Client
    participant AC as AuthController
    participant AS as AuthService
    participant DB as Database

    Note over C,DB: 회원가입
    C->>AC: POST /auth/register
    AC->>AS: register(email, pw, name)
    AS->>DB: existsByEmail() 중복 확인
    AS->>DB: save(User)
    AS->>DB: save(RefreshToken)
    AS-->>AC: TokenResponse
    AC-->>C: 201 { accessToken, refreshToken }

    Note over C,DB: 로그인
    C->>AC: POST /auth/login
    AC->>AS: login(email, pw)
    AS->>DB: findByEmail()
    AS->>AS: BCrypt.matches()
    AS->>DB: save(RefreshToken)
    AS-->>AC: TokenResponse
    AC-->>C: 200 { accessToken, refreshToken }

    Note over C,DB: 토큰 갱신
    C->>AC: POST /auth/refresh
    AC->>AS: refresh(refreshToken)
    AS->>DB: findByToken() → 만료 확인
    AS->>DB: delete(old) + save(new)
    AS-->>AC: TokenResponse
    AC-->>C: 200 { accessToken, refreshToken }

    Note over C,DB: 로그아웃
    C->>AC: POST /auth/logout
    AC->>AS: logout(refreshToken)
    AS->>DB: delete(RefreshToken)
    AC-->>C: 200 OK
```

### 4-2. 보호된 리소스 접근 흐름

```mermaid
sequenceDiagram
    actor C as Client
    participant F as JwtAuthFilter
    participant JWT as JwtTokenProvider
    participant SC as SecurityContext
    participant UC as UserController
    participant US as UserService
    participant DB as Database

    C->>F: GET /users/me\nAuthorization: Bearer <token>
    F->>JWT: validateToken(token)
    alt 유효한 토큰
        JWT-->>F: true
        F->>JWT: getUserId(token)
        JWT-->>F: userId
        F->>SC: setAuthentication(userId)
        F->>UC: 요청 전달
        UC->>US: getProfile(userId)
        US->>DB: findById(userId)
        DB-->>US: User
        US-->>UC: UserProfileResponse
        UC-->>C: 200 { id, email, name, ... }
    else 유효하지 않은 토큰
        JWT-->>F: false
        F->>UC: 인증 없이 전달
        UC-->>C: 401 Unauthorized
    end
```

### 4-3. 친구 흐름

```mermaid
sequenceDiagram
    actor C as Client
    participant FC as FriendController
    participant FS as FriendService
    participant DB as Database

    Note over C,DB: 친구 요청
    C->>FC: POST /friends/request/{targetId}
    FC->>FS: sendFriendRequest(requesterId, targetId)
    FS->>DB: existsByRequesterAndReceiver() 중복 확인
    FS->>DB: save(Friend{PENDING})
    FS-->>FC: FriendStatusResponse
    FC-->>C: 201 { id, status: PENDING }

    Note over C,DB: 친구 수락
    C->>FC: PATCH /friends/{friendId}/accept
    FC->>FS: acceptFriend(friendId, userId)
    FS->>DB: findById(friendId)
    FS->>FS: friend.accept() → ACCEPTED
    FS-->>FC: FriendStatusResponse
    FC-->>C: 200 { id, status: ACCEPTED }

    Note over C,DB: 친구 차단
    C->>FC: PATCH /friends/{friendId}/block
    FC->>FS: blockFriend(friendId, userId)
    FS->>DB: findById(friendId)
    FS->>FS: friend.block() → BLOCKED
    FS-->>FC: FriendStatusResponse
    FC-->>C: 200 { id, status: BLOCKED }
```

### 4-4. 채팅방 흐름

```mermaid
sequenceDiagram
    actor C as Client
    participant CC as ChatRoomController
    participant CS as ChatRoomService
    participant DB as Database

    Note over C,DB: 1:1 채팅방 생성
    C->>CC: POST /chat-rooms/direct
    CC->>CS: createDirectRoom(userId, targetId)
    CS->>DB: findExistingDirect() 기존 방 확인
    CS->>DB: save(ChatRoom{DIRECT})
    CS->>DB: save(ChatRoomMember × 2)
    CS-->>CC: ChatRoomResponse
    CC-->>C: 201 { id, type: DIRECT, ... }

    Note over C,DB: 그룹 채팅방 생성
    C->>CC: POST /chat-rooms/group
    CC->>CS: createGroupRoom(userId, request)
    CS->>DB: save(ChatRoom{GROUP, name})
    CS->>DB: save(ChatRoomMember{OWNER})
    CS->>DB: save(ChatRoomMember{MEMBER} × N)
    CS-->>CC: ChatRoomResponse
    CC-->>C: 201 { id, type: GROUP, name, ... }

    Note over C,DB: 멤버 초대
    C->>CC: POST /chat-rooms/{roomId}/members
    CC->>CS: inviteMember(userId, roomId, targetId)
    CS->>DB: findById(roomId) 권한 확인
    CS->>DB: save(ChatRoomMember{MEMBER})
    CC-->>C: 201 Created

    Note over C,DB: 채팅방 나가기
    C->>CC: DELETE /chat-rooms/{roomId}/members/me
    CC->>CS: leaveChatRoom(userId, roomId)
    CS->>DB: findMember() → leftAt 설정
    CC-->>C: 204 No Content
```

---

## 5. 구현 현황

| Phase | 도메인 | 상태 |
|-------|--------|------|
| 1-1 | User 엔티티 · Repository | ✅ 완료 |
| 1-2 | 인증 (JWT · BCrypt · Refresh Token) | ✅ 완료 |
| 1-3 | 회원 API (GET /users/me, PATCH /users/me) | ✅ 완료 |
| 2 | 친구 (Friend 엔티티 · 요청/수락/차단/삭제 API) | ✅ 완료 |
| 3 | 채팅방 (ChatRoom · ChatRoomMember · 1:1/그룹 API) | ✅ 완료 |
| 4 | 메시지 · WebSocket | ⬜ 미구현 |
| 5 | 알림 (Notification) | ⬜ 미구현 |
| 6 | 인프라 (MySQL · Redis · Docker) | ⬜ 미구현 |

## 6. API 엔드포인트 목록

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /auth/register | 회원가입 | ❌ |
| POST | /auth/login | 로그인 | ❌ |
| POST | /auth/refresh | 토큰 갱신 | ❌ |
| POST | /auth/logout | 로그아웃 | ✅ |
| GET | /users/me | 내 프로필 조회 | ✅ |
| PATCH | /users/me | 내 프로필 수정 | ✅ |
| GET | /friends | 친구 목록 조회 | ✅ |
| GET | /friends/requests | 받은 친구 요청 목록 | ✅ |
| POST | /friends/request/{targetId} | 친구 요청 전송 | ✅ |
| PATCH | /friends/{friendId}/accept | 친구 요청 수락 | ✅ |
| PATCH | /friends/{friendId}/reject | 친구 요청 거절 | ✅ |
| PATCH | /friends/{friendId}/block | 친구 차단 | ✅ |
| DELETE | /friends/{friendId} | 친구 삭제 | ✅ |
| POST | /chat-rooms/direct | 1:1 채팅방 생성 | ✅ |
| POST | /chat-rooms/group | 그룹 채팅방 생성 | ✅ |
| GET | /chat-rooms | 내 채팅방 목록 | ✅ |
| GET | /chat-rooms/{roomId} | 채팅방 상세 조회 | ✅ |
| PATCH | /chat-rooms/{roomId} | 채팅방 이름 수정 | ✅ |
| POST | /chat-rooms/{roomId}/members | 멤버 초대 | ✅ |
| DELETE | /chat-rooms/{roomId}/members/me | 채팅방 나가기 | ✅ |
| DELETE | /chat-rooms/{roomId}/members/{targetUserId} | 멤버 강퇴 (OWNER) | ✅ |