package hello.connectme.domain.user;

import hello.connectme.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * 로컬 이메일 가입과 소셜(Google, Apple) 로그인 모두 지원
 * 논리 삭제(soft delete) 방식으로 탈퇴 처리
 */
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 500)
    private String statusMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(length = 255)
    private String providerId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 회원 탈퇴 여부 확인
     * @return 삭제된 경우 true
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 논리 삭제 처리 — 현재 시각을 deletedAt에 기록
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 이메일/비밀번호 방식 로컬 회원 생성 팩토리 메서드
     * @param email 이메일 주소
     * @param phoneNumber 전화번호 (선택)
     * @param name 이름
     * @param passwordHash BCrypt 암호화된 비밀번호
     * @return 생성된 User 엔티티
     */
    public static User createLocal(String email, String phoneNumber, String name, String passwordHash) {
        User user = new User();
        user.email = email;
        user.phoneNumber = phoneNumber;
        user.name = name;
        user.passwordHash = passwordHash;
        user.provider = AuthProvider.LOCAL;
        return user;
    }

    /**
     * 소셜 로그인 회원 생성 팩토리 메서드
     * @param email 이메일 주소
     * @param name 이름
     * @param provider 소셜 인증 제공자
     * @param providerId 소셜 제공자에서 발급한 고유 ID
     * @return 생성된 User 엔티티
     */
    public static User createSocial(String email, String name, AuthProvider provider, String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    /**
     * 프로필 정보 수정 — null인 필드는 기존 값을 유지
     * @param name 변경할 이름 (null 시 유지)
     * @param statusMessage 변경할 상태 메시지 (null 시 유지)
     * @param profileImage 변경할 프로필 이미지 URL (null 시 유지)
     */
    public void updateProfile(String name, String statusMessage, String profileImage) {
        if (name != null) this.name = name;
        if (statusMessage != null) this.statusMessage = statusMessage;
        if (profileImage != null) this.profileImage = profileImage;
    }
}