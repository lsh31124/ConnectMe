# ConnectMe — 구현 TODO

도메인 의존 순서: 회원 → 친구 → 채팅방 → 메시지 → 알림

---

## Phase 0. 프론트엔드 (Frontend)

### FE-1. 기반 세팅 ✅
- [x] React 18 + Vite 5 프로젝트 세팅 (`connect-me-frontend`)
- [x] React Router v6 라우팅 구성
- [x] Vite 개발 서버 → Spring Boot API 프록시 (`/auth`, `/users` → 8080)

### FE-2. 인증 화면 ✅
- [x] 로그인 페이지 (`/login`) — screenshots/login.png 기준 UI
- [x] 회원가입 페이지 (`/register`)
- [x] API 연동 — `POST /auth/login`, `POST /auth/register`
- [x] JWT 토큰 관리 (localStorage + Zustand persist)
- [x] 로그인 상태 보호 라우트 (`PrivateRoute`)

### FE-3. 회원 화면 ✅
- [x] 내 프로필 페이지 (`GET /users/me`)
- [x] 프로필 수정 (`PATCH /users/me`)

### FE-4. 친구 화면 ✅
- [x] 친구 목록 / 친구 요청 목록
- [x] 친구 요청 수락 · 거절

### FE-5. 채팅 화면
- [x] 채팅방 목록 (`/chats` — ChatListPage, chatApi.js)
- [ ] 채팅방 (WebSocket STOMP 실시간)
- [ ] 이미지 / 파일 첨부 전송

---

## Phase 1. 회원 (User)

### 1-1. 도메인 레이어 ✅
- [x] `User` 엔티티 (`connect-me-domain`)
- [x] `UserRepository`
- [x] `AuthProvider` enum (LOCAL / GOOGLE / APPLE)
- [x] `BaseTimeEntity` (createdAt / updatedAt)

### 1-2. 인증 (Auth) ✅
- [x] `POST /auth/register` — 로컬 회원가입 (이메일 + 비밀번호)
- [x] `POST /auth/login` — 로그인 → JWT 발급 (Access + Refresh Token)
- [x] `POST /auth/refresh` — Access Token 재발급
- [x] `POST /auth/logout` — Refresh Token 폐기
- [ ] `POST /auth/oauth/{provider}` — 소셜 로그인 (Google / Apple)
- [x] JWT 필터 (`JwtAuthenticationFilter`) 및 `SecurityConfig`
- [x] 비밀번호 BCrypt 해싱

### 1-3. 회원 API ✅
- [x] `GET /users/me` — 내 프로필 조회
- [x] `PATCH /users/me` — 프로필 수정 (이름 / 상태메시지 / 프로필 이미지)
- [x] `GET /users?query=` — 이메일 또는 전화번호로 회원 검색
- [x] `DELETE /users/me` — 회원 탈퇴 (soft delete)

---

## Phase 2. 친구 (Friend)

### 2-1. 도메인 레이어 ✅
- [x] `Friend` 엔티티 (requester_id, receiver_id, status)
- [x] `FriendStatus` enum (PENDING / ACCEPTED / BLOCKED)
- [x] `FriendRepository`

### 2-2. 친구 API ✅
- [x] `POST /friends/request/{targetId}` — 친구 요청 (PENDING)
- [x] `PATCH /friends/{friendId}/accept` — 친구 수락 (PENDING → ACCEPTED)
- [x] `PATCH /friends/{friendId}/reject` — 친구 거절 (PENDING 삭제)
- [x] `PATCH /friends/{friendId}/block` — 차단 (ACCEPTED → BLOCKED)
- [x] `DELETE /friends/{friendId}` — 친구 삭제
- [x] `GET /friends` — 내 친구 목록
- [x] `GET /friends/requests` — 받은 친구 요청 목록

---

## Phase 3. 채팅방 (Chat Room)

### 3-1. 도메인 레이어 ✅
- [x] `ChatRoom` 엔티티 (name, type, created_by, pinned_msg_id)
- [x] `ChatRoomType` enum (DIRECT / GROUP)
- [x] `ChatRoomMember` 엔티티 (role, is_pinned, joined_at, left_at)
- [x] `ChatRoomMemberRole` enum (OWNER / MEMBER)
- [x] `ChatRoomRepository`, `ChatRoomMemberRepository`

### 3-2. 채팅방 API ✅
- [x] `POST /chat-rooms/direct` — 1:1 채팅방 생성 (이미 있으면 기존 반환)
- [x] `POST /chat-rooms/group` — 그룹 채팅방 생성
- [x] `GET /chat-rooms` — 내 채팅방 목록 (최근 메시지 포함)
- [x] `GET /chat-rooms/{roomId}` — 채팅방 상세 (멤버 목록 포함)
- [x] `PATCH /chat-rooms/{roomId}` — 채팅방 이름 수정 (OWNER만)
- [x] `POST /chat-rooms/{roomId}/members` — 멤버 초대
- [x] `DELETE /chat-rooms/{roomId}/members/me` — 채팅방 나가기
- [x] `DELETE /chat-rooms/{roomId}/members/{userId}` — 멤버 강퇴 (OWNER만)

---

## Phase 4. 메시지 (Message)

### 4-1. 도메인 레이어 ✅
- [x] `Message` 엔티티 (type, content, file_url, file_name, file_size, is_deleted)
- [x] `MessageType` enum (TEXT / IMAGE / FILE)
- [x] `MessageRepository`

### 4-2. 실시간 메시지 (WebSocket / STOMP) ✅
- [x] WebSocket 엔드포인트 설정 (`/ws`)
- [x] STOMP 브로커 설정 (`/sub`, `/pub`)
- [x] `SEND /pub/chat/{roomId}` — 메시지 전송
- [x] `SUBSCRIBE /sub/chat/{roomId}` — 메시지 수신
- [x] WebSocket 인증 (JWT 핸드셰이크 인터셉터 — `JwtChannelInterceptor`)

### 4-3. 메시지 REST API
- [ ] `GET /chat-rooms/{roomId}/messages` — 메시지 히스토리 (커서 페이징)
- [ ] `DELETE /messages/{messageId}` — 메시지 삭제 (soft delete, `is_deleted=true`)
- [ ] `PATCH /chat-rooms/{roomId}/pin` — 메시지 핀 고정 (`pinned_msg_id` 업데이트)

### 4-4. 파일 첨부
- [ ] `POST /upload` — 이미지 / 파일 업로드 (S3 또는 로컬 스토리지)
- [ ] IMAGE / FILE 타입 메시지 전송

---

## Phase 5. 알림 (Notification)

### 5-1. 도메인 레이어
- [ ] `Notification` 엔티티 (type, ref_id, is_read)
- [ ] `NotificationType` enum (MESSAGE / FRIEND_REQUEST)
- [ ] `NotificationRepository`

### 5-2. 알림 API
- [ ] `GET /notifications` — 알림 목록 (읽지 않은 것 우선)
- [ ] `PATCH /notifications/{id}/read` — 알림 읽음 처리
- [ ] `PATCH /notifications/read-all` — 전체 읽음 처리

### 5-3. 실시간 알림
- [ ] SSE 또는 WebSocket으로 실시간 알림 푸시
- [ ] 친구 요청 수신 시 알림 생성
- [ ] 새 메시지 수신 시 알림 생성 (채팅방 밖에 있을 때)

---

## Phase 6. 인프라 / 운영

- [ ] MySQL 로 DataSource 전환 (현재 H2 개발용)
- [x] `application-prod.properties` 분리 (dev / prod 프로파일)
- [x] Logback 설정 (`logback-spring.xml` — dev 콘솔 DEBUG / prod 파일 WARN, 일별 롤링 30일)
- [x] 전역 예외 처리 `GlobalExceptionHandler` (`{"code":"CODE","message":"설명"}`)
- [x] API 공통 응답 래퍼 `ApiResponse<T>`
- [ ] Swagger / SpringDoc OpenAPI 문서 자동화
- [ ] Docker Compose (MySQL + Redis + 앱)
- [ ] Redis — Refresh Token 저장소 및 채팅 세션 캐시
- [ ] 단위 테스트 (Service 레이어) / 통합 테스트 (Repository 레이어)

---

## Phase 7. 어드민 (Admin)

- [ ] 어드민 전용 계정/권한 (`ROLE_ADMIN`)
- [ ] 회원 관리 (조회 / 정지 / 탈퇴)
- [ ] 신고 관리
- [ ] 채팅방 모니터링
- [ ] 통계 대시보드 (DAU, MAU)
- [ ] 어드민 페이지 UI (React)

---

## 배포

- [ ] Oracle Cloud Free tier 서버 세팅
- [ ] GitHub Actions CI/CD (PR → 자동 테스트, main 머지 → 자동 배포)
- [ ] Sentry Free tier 에러 추적 연동
- [ ] 토스 미니앱 WebView 출시 준비 (TDS 컴포넌트, 토스 SDK 연동)
<!-- Last Claude: 2026-05-20 16:16 -->
/