# CLAUDE.md
Spring Boot 4.0.5/Java 21 + React 18/Vite 5 메신저.

금지: .env 커밋, 프로덕션 DB 쿼리
백엔드 빌드: `./gradlew build|bootRun|test`
프론트 빌드: `cd connect-me-frontend && npm install && npm run dev`
클래스 PascalCase, 메서드/변수 camelCase
패키지: hello.connectme.*

## 모듈 구조
- connect-me-api: 메인 앱 (컨트롤러, 서비스) — 포트 8080
- connect-me-domain: 엔티티, 레포지토리
- connect-me-common: 공통 유틸, 예외처리
- connect-me-frontend: React SPA — 포트 3000, `/auth` `/users` → 8080 프록시

## 프론트엔드 규칙
- 컴포넌트 파일명 PascalCase, 함수명 camelCase
- 스타일: CSS Modules (*.module.css)
- 라우터: react-router-dom v6

## 도메인
회원→친구→채팅(1:1/그룹)→메시지→알림
에러: `{"code":"CODE","message":"설명"}`

## Git Flow 브랜치 전략
- main: 프로덕션 배포
- develop: 개발 통합 브랜치
- feature/기능명: 기능 개발 (develop에서 분기)
- release/버전: 배포 준비 (develop에서 분기)
- hotfix/이슈: 긴급 수정 (main에서 분기)

PR 규칙: feature/* → develop (직접 main 병합 금지)

## TDD 규칙
- 모든 기능 구현 전 테스트 코드 먼저 작성
- 테스트 실패 확인 후 구현 시작
- 구현 후 테스트 통과 확인 후 커밋
- 테스트 파일 위치: 각 모듈 src/test/java 하위
- 단위 테스트: JUnit5 + Mockito
- 통합 테스트: @SpringBootTest

[DB Schema](docs/db-schema.md) | [Architecture](docs/architecture.md)