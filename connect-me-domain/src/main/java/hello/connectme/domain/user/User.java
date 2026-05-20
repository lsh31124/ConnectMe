package hello.connectme.domain.user;

import hello.connectme.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

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

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public static User createLocal(String email, String phoneNumber, String name, String passwordHash) {
        User user = new User();
        user.email = email;
        user.phoneNumber = phoneNumber;
        user.name = name;
        user.passwordHash = passwordHash;
        user.provider = AuthProvider.LOCAL;
        return user;
    }

    public static User createSocial(String email, String name, AuthProvider provider, String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    public void updateProfile(String name, String statusMessage, String profileImage) {
        if (name != null) this.name = name;
        if (statusMessage != null) this.statusMessage = statusMessage;
        if (profileImage != null) this.profileImage = profileImage;
    }
}