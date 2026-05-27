package hello.connectme.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 회원 프로필 수정 요청 DTO
 * 모든 필드는 선택 입력이며 null인 경우 기존 값 유지
 */
public record UpdateProfileRequest(
        @Size(max = 50, message = "이름은 50자 이하여야 합니다") String name,
        @Size(max = 500, message = "상태 메시지는 500자 이하여야 합니다") String statusMessage,
        @Size(max = 2048) String profileImage
) {}