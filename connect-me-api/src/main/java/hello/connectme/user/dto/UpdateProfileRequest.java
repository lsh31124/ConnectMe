package hello.connectme.user.dto;

public record UpdateProfileRequest(
        String name,
        String statusMessage,
        String profileImage
) {}