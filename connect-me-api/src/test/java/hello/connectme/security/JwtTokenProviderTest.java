package hello.connectme.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 3600000L);
    }

    @Test
    void createAccessToken_returnsNonBlankToken() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertThat(token).isNotBlank();
    }

    @Test
    void getUserId_extractsCorrectUserId() {
        String token = jwtTokenProvider.createAccessToken(42L);

        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    void validateToken_returnsTrue_forValidToken() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalse_forMalformedToken() {
        assertThat(jwtTokenProvider.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forEmptyToken() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forExpiredToken() throws InterruptedException {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "secretKey", SECRET);
        ReflectionTestUtils.setField(expiredProvider, "accessTokenExpiry", 1L);

        String token = expiredProvider.createAccessToken(1L);
        Thread.sleep(10);

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    void createRefreshToken_returnsUuidFormat() {
        String token = jwtTokenProvider.createRefreshToken();

        assertThat(token).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void createRefreshToken_returnsDifferentValuesEachCall() {
        String token1 = jwtTokenProvider.createRefreshToken();
        String token2 = jwtTokenProvider.createRefreshToken();

        assertThat(token1).isNotEqualTo(token2);
    }
}