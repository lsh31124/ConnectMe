package hello.connectme.user.dto;

/**
 * 회원 프로필 수정 요청 DTO
 * 모든 필드는 선택 입력이며 null인 경우 기존 값 유지
 */
public record UpdateProfileRequest(
        String name,
        String statusMessage,
        String profileImage
) {}