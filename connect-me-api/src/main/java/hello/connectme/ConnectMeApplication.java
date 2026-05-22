package hello.connectme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ConnectMe 메신저 애플리케이션 진입점
 * JPA Auditing 활성화로 BaseTimeEntity의 자동 시각 기록 지원
 */
@SpringBootApplication
@EnableJpaAuditing
public class ConnectMeApplication {

    /**
     * 애플리케이션 시작 메서드
     * @param args 커맨드라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(ConnectMeApplication.class, args);
    }
}