package hello.connectme.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티가 상속하는 생성/수정 시각 자동 관리 기반 클래스
 * JPA Auditing을 통해 createdAt, updatedAt 자동 설정
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 엔티티 최초 생성 시각 반환
     * @return 생성 시각
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * 엔티티 최근 수정 시각 반환
     * @return 수정 시각
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}