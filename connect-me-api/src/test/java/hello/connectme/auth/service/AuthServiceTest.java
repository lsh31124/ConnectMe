package hello.connectme.auth.service;

import hello.connectme.auth.dto.LoginRequest;
import hello.connectme.auth.dto.RegisterRequest;
import hello.connectme.auth.dto.TokenResponse;
import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.auth.RefreshToken;
import hello.connectme.domain.auth.RefreshTokenRepository;
import hello.connectme.domain.user.User;
import hello.connectme.domain.user.UserRepository;
import hello.connectme.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("test@email.com", "password123", "홍길동", "01012345678");
        given(userRepository.existsByEmail(req.email())).willReturn(false);
        given(userRepository.existsByPhoneNumber(req.phoneNumber())).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 1L);
            return u;
        });
        given(passwordEncoder.encode(req.password())).willReturn("encodedPw");
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken()).willReturn("refresh-token");

        TokenResponse response = authService.register(req);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest req = new RegisterRequest("dup@email.com", "password123", "홍길동", null);
        given(userRepository.existsByEmail(req.email())).willReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    void register_duplicatePhone_throwsException() {
        RegisterRequest req = new RegisterRequest("test@email.com", "password123", "홍길동", "01011112222");
        given(userRepository.existsByEmail(req.email())).willReturn(false);
        given(userRepository.existsByPhoneNumber(req.phoneNumber())).willReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_PHONE);
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest("test@email.com", "password123");
        User user = User.createLocal("test@email.com", null, "홍길동", "encodedPw");
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findByEmail(req.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.password(), user.getPasswordHash())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken()).willReturn("refresh-token");

        TokenResponse response = authService.login(req);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_userNotFound_throwsInvalidPassword() {
        LoginRequest req = new LoginRequest("notfound@email.com", "password");
        given(userRepository.findByEmail(req.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    void login_wrongPassword_throwsInvalidPassword() {
        LoginRequest req = new LoginRequest("test@email.com", "wrongPw");
        User user = User.createLocal("test@email.com", null, "홍길동", "encodedPw");
        given(userRepository.findByEmail(req.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.password(), user.getPasswordHash())).willReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    void refresh_success() {
        String rawToken = "valid-refresh-token";
        RefreshToken stored = RefreshToken.of(1L, rawToken, LocalDateTime.now().plusDays(7));
        given(refreshTokenRepository.findByToken(rawToken)).willReturn(Optional.of(stored));
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken()).willReturn("new-refresh-token");

        TokenResponse response = authService.refresh(rawToken);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        then(refreshTokenRepository).should().delete(stored);
    }

    @Test
    void refresh_tokenNotFound_throwsException() {
        given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("ghost-token"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    void refresh_expiredToken_throwsException() {
        String rawToken = "expired-token";
        RefreshToken expired = RefreshToken.of(1L, rawToken, LocalDateTime.now().minusDays(1));
        given(refreshTokenRepository.findByToken(rawToken)).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh(rawToken))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);

        then(refreshTokenRepository).should().delete(expired);
    }

    @Test
    void logout_deletesToken_whenExists() {
        String rawToken = "some-token";
        RefreshToken stored = RefreshToken.of(1L, rawToken, LocalDateTime.now().plusDays(7));
        given(refreshTokenRepository.findByToken(rawToken)).willReturn(Optional.of(stored));

        authService.logout(rawToken);

        then(refreshTokenRepository).should().delete(stored);
    }

    @Test
    void logout_noOp_whenTokenNotFound() {
        given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> authService.logout("unknown-token"));
        then(refreshTokenRepository).should(never()).delete(any());
    }
}