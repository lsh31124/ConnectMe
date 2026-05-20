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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (request.phoneNumber() != null && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }
        User user = User.createLocal(
                request.email(), request.phoneNumber(),
                request.name(), passwordEncoder.encode(request.password())
        );
        userRepository.save(user);
        return issueTokens(user.getId());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PASSWORD));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return issueTokens(user.getId());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
        refreshTokenRepository.delete(stored);
        return issueTokens(stored.getUserId());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    private TokenResponse issueTokens(Long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String rawRefresh = jwtTokenProvider.createRefreshToken();
        refreshTokenRepository.save(RefreshToken.of(userId, rawRefresh, LocalDateTime.now().plusDays(7)));
        return new TokenResponse(accessToken, rawRefresh, "Bearer");
    }
}