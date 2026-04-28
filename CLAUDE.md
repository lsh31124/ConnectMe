# CLAUDE.md
Spring Boot 4.0.5/Java 21 메신저.

금지: .env 커밋, 프로덕션 DB 쿼리
빌드: `./gradlew build|bootRun|test`
클래스 PascalCase, 메서드/변수 camelCase
패키지: hello.sns_project.*
도메인: 회원→친구→채팅(1:1/그룹)→메시지→알림
에러: `{"code":"CODE","message":"설명"}`

[DB Schema](docs/db-schema.md)