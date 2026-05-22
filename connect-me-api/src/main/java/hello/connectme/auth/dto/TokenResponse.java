package hello.connectme.auth.dto;

/**
 * 인증 토큰 응답 DTO
 * 로그인/회원가입/토큰 갱신 성공 시 반환되며 tokenType은 항상 "Bearer"
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}