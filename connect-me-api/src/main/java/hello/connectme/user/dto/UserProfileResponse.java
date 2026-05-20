package hello.connectme.user.dto;

import hello.connectme.domain.user.User;

public record UserProfileResponse(
        Long id,
        String email,
        String phoneNumber,
        String name,
        String profileImage,
        String statusMessage,
        String provider
) {
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