package hello.connectme.friend.dto;

import hello.connectme.domain.friend.Friend;
import hello.connectme.domain.user.User;

public record FriendSummaryResponse(
        Long id,
        Long userId,
        String name,
        String email
) {
    public static FriendSummaryResponse from(Friend friend, User partner) {
        return new FriendSummaryResponse(
                friend.getId(),
                partner.getId(),
                partner.getName(),
                partner.getEmail()
        );
    }
}