package hello.connectme.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 * 이메일과 비밀번호 모두 필수
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {}