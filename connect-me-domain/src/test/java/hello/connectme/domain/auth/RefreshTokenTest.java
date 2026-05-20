package hello.connectme.domain.auth;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    void of_setsAllFields() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);

        RefreshToken rt = RefreshToken.of(1L, "some-token-value", expiry);

        assertThat(rt.getUserId()).isEqualTo(1L);
        assertThat(rt.getToken()).isEqualTo("some-token-value");
        assertThat(rt.getExpiryDate()).isEqualTo(expiry);
    }

    @Test
    void isExpired_returnsFalse_whenExpiryIsInFuture() {
        RefreshToken rt = RefreshToken.of(1L, "token", LocalDateTime.now().plusDays(1));

        assertThat(rt.isExpired()).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenExpiryIsInPast() {
        RefreshToken rt = RefreshToken.of(1L, "token", LocalDateTime.now().minusSeconds(1));

        assertThat(rt.isExpired()).isTrue();
    }

    @Test
    void isExpired_returnsTrue_justExpired() {
        RefreshToken rt = RefreshToken.of(1L, "token", LocalDateTime.now().minusDays(7));

        assertThat(rt.isExpired()).isTrue();
    }
}