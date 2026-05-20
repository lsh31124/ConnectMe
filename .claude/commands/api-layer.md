---
description: TDD 기반으로 API 레이어(DTO + Service + Controller + Tests)를 생성합니다. 사용법: /api-layer <DomainName> [설명]
---

# api-layer 스킬

$ARGUMENTS 에서 도메인 이름(첫 번째 단어, PascalCase)과 설명(나머지)을 파싱한다.

---

## 실행 전 — 컨텍스트 파악

다음 파일을 읽어 프로젝트 패턴을 파악한다:
- `connect-me-api/src/main/java/hello/connectme/user/service/UserService.java` — 서비스 패턴
- `connect-me-api/src/main/java/hello/connectme/user/controller/UserController.java` — 컨트롤러 패턴
- `connect-me-api/src/test/java/hello/connectme/user/service/UserServiceTest.java` — 서비스 테스트 패턴
- `connect-me-api/src/test/java/hello/connectme/user/controller/UserControllerIntegrationTest.java` — 통합 테스트 패턴
- `connect-me-common/src/main/java/hello/connectme/common/exception/ErrorCode.java` — 에러코드 현황
- 해당 도메인 엔티티 (`connect-me-domain/src/main/java/hello/connectme/domain/<소문자>/`)

---

## Step 1 — 설계 (구현 전 명시)

코드 작성 전 사용자에게 보여준다:

```
도메인: <DomainName>
패키지: hello.connectme.<소문자도메인>

DTO:
  - <DomainName>Response  (record)
  - <RequestName>Request  (record, 엔드포인트마다)

Service 메서드:
  - <메서드 목록 — 시그니처 포함>

Controller 엔드포인트:
  - METHOD /path — 설명 → HTTP 상태코드

Security:
  - 인증 필요 여부 (JWT Bearer 토큰)
  - SecurityConfig 허용 경로 추가 여부

서비스 단위 테스트:
  - <메서드명>_success
  - <메서드명>_<조건>_throwsException  (예외 케이스마다)

컨트롤러 통합 테스트:
  - <엔드포인트>_success_returns<상태코드>
  - <엔드포인트>_withoutToken_returnsForbidden  (인증 필요 시)
  - <엔드포인트>_<오류케이스>
```

---

## Step 2 — TDD: 테스트 먼저 작성 (Red)

### 2-1. 서비스 단위 테스트
파일: `connect-me-api/src/test/java/hello/connectme/<domain>/service/<DomainName>ServiceTest.java`

규칙:
- `@ExtendWith(MockitoExtension.class)` — Spring context 없음
- `@InjectMocks` / `@Mock` 의존성 주입
- BDD 스타일: `given(...).willReturn(...)`, `then(...).should(...)`
- ID 주입: `ReflectionTestUtils.setField(entity, "id", 1L)`
- 예외 검증: `.extracting(e -> ((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.xxx)`
- `import *` 금지 — 명시적 import

```java
@ExtendWith(MockitoExtension.class)
class <DomainName>ServiceTest {

    @InjectMocks
    private <DomainName>Service <camel>Service;

    @Mock
    private <DomainName>Repository <camel>Repository;
    // 필요한 다른 Repository mock

    // 헬퍼: 테스트용 엔티티 생성
    private <EntityName> createTest<Entity>(Long id, ...) {
        <EntityName> entity = <EntityName>.create(...);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    void <method>_success() { ... }

    @Test
    void <method>_<condition>_throwsException() {
        // given: mock 설정
        // when/then: assertThatThrownBy + ErrorCode 검증
    }
}
```

### 2-2. 컨트롤러 통합 테스트
파일: `connect-me-api/src/test/java/hello/connectme/<domain>/controller/<DomainName>ControllerIntegrationTest.java`

규칙:
- `@SpringBootTest` + `@AutoConfigureMockMvc` (import: `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`)
- `@Transactional` — 테스트 후 롤백
- `tools.jackson.databind.ObjectMapper` (Spring Boot 4.x)
- `@BeforeEach` 에서 `/auth/register` → `/auth/login` 으로 accessToken 획득
- 인증 헤더: `.header("Authorization", "Bearer " + accessToken)`
- 응답 검증: `jsonPath("$.code").value("SUCCESS")`, `jsonPath("$.data.xxx").value(...)`
- 인증 없는 요청: `status().isForbidden()` (HTTP 403)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class <DomainName>ControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, String> body = Map.of(
            "email", "test@email.com",
            "password", "password123",
            "name", "홍길동"
        );
        String result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andReturn().getResponse().getContentAsString();
        accessToken = objectMapper.readTree(result)
            .path("data").path("accessToken").asText();
    }

    @Test
    void <endpoint>_success_returns<Status>() throws Exception { ... }

    @Test
    void <endpoint>_withoutToken_returnsForbidden() throws Exception { ... }
}
```

---

## Step 3 — 구현 (Green)

### 3-1. DTO (Record)
파일: `connect-me-api/src/main/java/hello/connectme/<domain>/dto/<Name>.java`

```java
package hello.connectme.<domain>.dto;

public record <DomainName>Response(
    Long id,
    // 응답 필드
) {
    public static <DomainName>Response from(<EntityName> entity) {
        return new <DomainName>Response(entity.getId(), ...);
    }
}
```

Request DTO:
```java
public record <ActionName>Request(
    // 요청 필드 (nullable 필드는 허용)
) {}
```

### 3-2. Service
파일: `connect-me-api/src/main/java/hello/connectme/<domain>/service/<DomainName>Service.java`

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class <DomainName>Service {

    private final <DomainName>Repository <camel>Repository;
    // 필요한 다른 Repository

    // 조회 메서드 — @Transactional 없음 (readOnly = true 상속)
    public <ReturnType> <method>(Long userId, ...) {
        // 검증 → 비즈니스 로직 → DTO 변환 반환
    }

    // 변경 메서드
    @Transactional
    public <ReturnType> <method>(Long userId, ...) {
        // 검증 → 도메인 메서드 호출 → DTO 변환 반환
    }
}
```

예외 발생 패턴:
```java
.orElseThrow(() -> new BusinessException(ErrorCode.<DOMAIN>_NOT_FOUND))
if (condition) throw new BusinessException(ErrorCode.<DOMAIN>_INVALID_STATUS);
```

### 3-3. Controller
파일: `connect-me-api/src/main/java/hello/connectme/<domain>/controller/<DomainName>Controller.java`

```java
@RestController
@RequestMapping("/<path>")
@RequiredArgsConstructor
public class <DomainName>Controller {

    private final <DomainName>Service <camel>Service;

    // 인증 불필요 엔드포인트
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<<DomainName>Response>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(<camel>Service.get(id)));
    }

    // 인증 필요 엔드포인트
    @PostMapping
    public ResponseEntity<ApiResponse<<DomainName>Response>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody <CreateName>Request request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(<camel>Service.create(userId, request)));
    }
}
```

HTTP 상태코드 기준:
- 200 OK: 조회, 수정, 상태 변경
- 201 Created: 신규 생성 (`POST`)
- 204 No Content: 삭제 (`ResponseEntity<Void>`)
- 403 Forbidden: 인증 없는 요청 (Spring Security 자동)

### 3-4. SecurityConfig 허용 경로 (필요 시)
인증 불필요 엔드포인트가 있으면 `SecurityConfig`의 `permitAll()` 목록에 추가한다.

---

## Step 4 — 검증

모든 파일 생성 후 출력:

```
생성된 파일:
  ✅ dto/<DomainName>Response.java
  ✅ dto/<ActionName>Request.java  (엔드포인트 수만큼)
  ✅ service/<DomainName>Service.java
  ✅ controller/<DomainName>Controller.java
  ✅ test/service/<DomainName>ServiceTest.java
  ✅ test/controller/<DomainName>ControllerIntegrationTest.java

테스트 실행:
  ./gradlew :connect-me-api:test --no-daemon

다음 단계:
  FE에서 /react-page 또는 src/api/<domain>Api.js 연동 구현
```

---

## 주의사항

- `tools.jackson.databind.ObjectMapper` — Spring Boot 4.x (기존 `com.fasterxml` 아님)
- `@AutoConfigureMockMvc` import: `org.springframework.boot.webmvc.test.autoconfigure`
- DTO는 `record` 사용 (`class` 금지)
- `@Data`, `@Setter` 금지 — record 불변성 활용
- `import *` 금지 — 명시적 import
- 서비스 테스트: Spring context 없음 (`@SpringBootTest` 금지)
- 통합 테스트: `@Transactional` 필수 (DB 상태 롤백)
- 컨트롤러에서 userId 추출: `Long.parseLong(userDetails.getUsername())`
- 에러 응답: `GlobalExceptionHandler`가 `BusinessException` → `{"code":"DOMAIN_001","message":"..."}` 자동 변환