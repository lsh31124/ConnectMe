---
description: TDD 기반으로 도메인 엔티티(Enum + Entity + Repository + Test)를 생성합니다. 사용법: /domain-entity <EntityName> [설명]
---

# domain-entity 스킬

$ARGUMENTS 에서 엔티티 이름(첫 번째 단어)과 설명(나머지)을 파싱한다.

## 실행 전 — 컨텍스트 파악

다음 파일을 읽어 프로젝트 패턴을 파악한다:
- `connect-me-domain/src/main/java/hello/connectme/domain/user/User.java` — 엔티티 패턴
- `connect-me-domain/src/main/java/hello/connectme/domain/common/BaseTimeEntity.java` — 베이스 클래스
- `connect-me-common/src/main/java/hello/connectme/common/exception/ErrorCode.java` — 에러코드 현황
- `connect-me-domain/src/test/java/hello/connectme/domain/user/UserTest.java` — 테스트 패턴

---

## Step 1 — 설계 (구현 전 명시)

아래 항목을 코드 작성 전에 사용자에게 보여준다:

```
엔티티: <EntityName>
패키지: hello.connectme.domain.<소문자패키지>
테이블: <snake_case_복수형>

필드:
  - id (PK, auto increment)
  - <도메인에 맞는 필드 목록>
  - (soft delete 대상이면) deletedAt

상태 Enum: <EntityName>Status (필요 시)
  - <상태값들>

팩토리 메서드: <EntityName>.create(...)
상태 전이 메서드: (있을 경우 목록)

Repository 메서드:
  - <도메인에 필요한 쿼리 메서드>

ErrorCode 추가:
  - <DOMAIN>_NOT_FOUND
  - (필요 시 추가)

테스트 케이스:
  - create_setsAllFields
  - (상태 전이마다 1개씩)
  - (엣지 케이스)
```

---

## Step 2 — TDD: 테스트 먼저 작성 (Red)

파일 위치: `connect-me-domain/src/test/java/hello/connectme/domain/<package>/<EntityName>Test.java`

규칙:
- 순수 JUnit5 (Spring context 없음 — `new` 객체로 직접 테스트)
- AssertJ `assertThat` 사용
- 메서드명: `동사_조건_결과` 형식 (예: `create_setsAllFields`, `accept_changesPendingToAccepted`)
- `@SpringBootTest` 금지 (도메인 단위 테스트)
- 코멘트 없이 선언적으로 작성

```java
package hello.connectme.domain.<package>;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class <EntityName>Test {

    @Test
    void create_setsAllFields() {
        // 팩토리 메서드 호출 → 각 필드 assertThat 검증
    }

    // 상태 전이마다 테스트 1개
    // 예: void accept_changesPendingToAccepted()

    // soft delete 대상이면:
    // void softDelete_setsDeletedAt()
    // void isDeleted_returnsFalseByDefault()
}
```

---

## Step 3 — 구현 (Green)

### 3-1. Enum (상태가 있는 경우)
파일: `connect-me-domain/src/main/java/hello/connectme/domain/<package>/<EntityName>Status.java`

```java
package hello.connectme.domain.<package>;

public enum <EntityName>Status {
    <상태값들>
}
```

### 3-2. Entity
파일: `connect-me-domain/src/main/java/hello/connectme/domain/<package>/<EntityName>.java`

필수 규칙:
- `extends BaseTimeEntity`
- `@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)`
- `@Entity @Table(name = "<snake_case_복수형>")`
- soft delete 대상이면 `@SQLRestriction("deleted_at IS NULL")` 추가 (import: `org.hibernate.annotations.SQLRestriction`)
- public 생성자 금지 — 팩토리 메서드만 사용
- 연관 엔티티 FK는 `Long <entityName>Id` 로 저장 (N+1 방지, 단순 참조)

```java
package hello.connectme.domain.<package>;

import hello.connectme.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
// soft delete 시: import org.hibernate.annotations.SQLRestriction;
// soft delete 시: import java.time.LocalDateTime;

@Entity
@Table(name = "<table_name>")
// @SQLRestriction("deleted_at IS NULL")  ← soft delete 시만
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class <EntityName> extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드 선언

    public static <EntityName> create(...) {
        <EntityName> entity = new <EntityName>();
        // 필드 설정
        return entity;
    }

    // 상태 전이 메서드 (있을 경우)

    // soft delete 시:
    // public boolean isDeleted() { return deletedAt != null; }
    // public void softDelete() { this.deletedAt = LocalDateTime.now(); }
}
```

### 3-3. Repository
파일: `connect-me-domain/src/main/java/hello/connectme/domain/<package>/<EntityName>Repository.java`

```java
package hello.connectme.domain.<package>;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface <EntityName>Repository extends JpaRepository<<EntityName>, Long> {
    // 도메인에 필요한 쿼리 메서드
    // JPQL이 필요한 경우 @Query + @Param 사용
}
```

### 3-4. ErrorCode 추가
`connect-me-common/src/main/java/hello/connectme/common/exception/ErrorCode.java` 에 추가:

```java
<ENTITY>_NOT_FOUND("<PREFIX>_001", "<엔티티명>을(를) 찾을 수 없습니다."),
// 도메인에 필요한 추가 에러코드
```

---

## Step 4 — 검증

모든 파일 생성 후 아래를 출력한다:

```
생성된 파일:
  ✅ <EntityName>Status.java   (Enum)
  ✅ <EntityName>.java          (Entity)
  ✅ <EntityName>Repository.java (Repository)
  ✅ <EntityName>Test.java      (단위 테스트)
  ✅ ErrorCode.java             (에러코드 추가)

테스트 실행:
  ./gradlew :connect-me-domain:test --no-daemon

다음 단계:
  connect-me-api 에서 <EntityName>Service + <EntityName>Controller 구현
  (TDD: 서비스 단위 테스트 → 통합 테스트 → 구현 순서)
```

---

## 주의사항

- 코멘트(주석) 작성 금지 — 코드 자체가 설명되어야 함
- `@Data`, `@Setter` 사용 금지 — 불변성 유지
- `import *` 금지 — 명시적 import
- Spring Boot 4.x 기준: Jackson → `tools.jackson.*`, `@AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure`
- 테스트는 반드시 실패(Red) 상태로 작성 후 구현(Green) 순서 준수