package hello.connectme.user.dto;

import hello.connectme.domain.user.User;

/**
 * 회원 프로필 응답 DTO
 * 회원의 공개 프로필 정보를 담아 클라이언트에 반환
 */
public record UserProfileResponse(
        Long id,
        String email,
        String phoneNumber,
        String name,
        String profileImage,
        String statusMessage,
        String provider
) {
    /**
     * User 엔티티를 UserProfileResponse로 변환하는 정적 팩토리 메서드
     * @param user 변환할 User 엔티티
     * @return UserProfileResponse 인스턴스
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getName(),
                user.getProfileImage(),
                user.getStatusMessage(),
                user.getProvider().name()
        );
    }
}