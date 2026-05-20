package hello.connectme.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void createLocal_setsAllFields() {
        User user = User.createLocal("test@email.com", "01012345678", "홍길동", "hashedPw");

        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPw");
        assertThat(user.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.getProviderId()).isNull();
    }

    @Test
    void createLocal_withNullPhoneNumber() {
        User user = User.createLocal("test@email.com", null, "홍길동", "hashedPw");

        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    void createSocial_setsAllFields() {
        User user = User.createSocial("social@email.com", "구글유저", AuthProvider.GOOGLE, "google-id-123");

        assertThat(user.getEmail()).isEqualTo("social@email.com");
        assertThat(user.getName()).isEqualTo("구글유저");
        assertThat(user.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.getProviderId()).isEqualTo("google-id-123");
        assertThat(user.getPasswordHash()).isNull();
    }

    @Test
    void updateProfile_updatesAllNonNullFields() {
        User user = User.createLocal("test@email.com", null, "원래이름", "pw");

        user.updateProfile("새이름", "상태메시지", "image.jpg");

        assertThat(user.getName()).isEqualTo("새이름");
        assertThat(user.getStatusMessage()).isEqualTo("상태메시지");
        assertThat(user.getProfileImage()).isEqualTo("image.jpg");
    }

    @Test
    void updateProfile_ignoresNullFields() {
        User user = User.createLocal("test@email.com", null, "원래이름", "pw");
        user.updateProfile("기존이름", "기존상태", "기존이미지.jpg");

        user.updateProfile(null, null, null);

        assertThat(user.getName()).isEqualTo("기존이름");
        assertThat(user.getStatusMessage()).isEqualTo("기존상태");
        assertThat(user.getProfileImage()).isEqualTo("기존이미지.jpg");
    }

    @Test
    void updateProfile_partialUpdate_onlyChangesNonNullFields() {
        User user = User.createLocal("test@email.com", null, "기존이름", "pw");
        user.updateProfile("이름", "상태", "이미지.jpg");

        user.updateProfile("새이름", null, null);

        assertThat(user.getName()).isEqualTo("새이름");
        assertThat(user.getStatusMessage()).isEqualTo("상태");
        assertThat(user.getProfileImage()).isEqualTo("이미지.jpg");
    }

    @Test
    void isDeleted_returnsFalseByDefault() {
        User user = User.createLocal("test@email.com", null, "홍길동", "pw");

        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    void softDelete_setsDeletedAt() {
        User user = User.createLocal("test@email.com", null, "홍길동", "pw");

        user.softDelete();

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
    }
}