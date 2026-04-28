# CLAUDE.md
Spring Boot 4.0.5/Java 21 메신저.

금지: .env 커밋, 프로덕션 DB 쿼리
빌드: `./gradlew build|bootRun|test`
클래스 PascalCase, 메서드/변수 camelCase
패키지: hello.connectme.*

## 모듈 구조
- connect-me-api: 메인 앱 (컨트롤러, 서비스)
- connect-me-domain: 엔티티, 레포지토리
- connect-me-common: 공통 유틸, 예외처리

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

[DB Schema](docs/db-schema.md)