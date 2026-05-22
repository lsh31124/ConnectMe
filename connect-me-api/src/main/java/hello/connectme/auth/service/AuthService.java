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

/**
 * 인증 비즈니스 로직 서비스
 * 회원 가입, 로그인, 토큰 갱신, 로그아웃 처리
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신규 회원 가입 처리 — 이메일/전화번호 중복 검사 후 저장
     * @param request 회원 가입 요청 정보
     * @return 발급된 액세스/리프레시 토큰
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        // 전화번호 중복 검사 (입력한 경우에만)
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

    /**
     * 이메일/비밀번호 로그인 처리
     * @param request 로그인 요청 정보
     * @return 발급된 액세스/리프레시 토큰
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일로 회원 조회 — 없으면 INVALID_PASSWORD로 응답 (계정 존재 여부 노출 방지)
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PASSWORD));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return issueTokens(user.getId());
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     * @param refreshToken 기존 리프레시 토큰
     * @return 새로 발급된 액세스/리프레시 토큰
     */
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        // 만료된 리프레시 토큰 삭제 후 예외 발생
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
        // 기존 리프레시 토큰 삭제 후 새 토큰 발급 (1회용)
        refreshTokenRepository.delete(stored);
        return issueTokens(stored.getUserId());
    }

    /**
     * 로그아웃 처리 — 리프레시 토큰 삭제
     * @param refreshToken 삭제할 리프레시 토큰
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 함께 발급하는 내부 메서드
     * @param userId 토큰을 발급받을 회원 ID
     * @return TokenResponse 토큰 응답
     */
    private TokenResponse issueTokens(Long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String rawRefresh = jwtTokenProvider.createRefreshToken();
        // 리프레시 토큰 7일 유효기간으로 DB 저장
        refreshTokenRepository.save(RefreshToken.of(userId, rawRefresh, LocalDateTime.now().plusDays(7)));
        return new TokenResponse(accessToken, rawRefresh, "Bearer");
    }
}