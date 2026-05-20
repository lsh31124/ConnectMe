package hello.connectme.friend.dto;

import hello.connectme.domain.friend.Friend;

public record FriendStatusResponse(
        Long id,
        Long requesterId,
        Long receiverId,
        String status
) {
    public static FriendStatusResponse from(Friend friend) {
        return new FriendStatusResponse(
                friend.getId(),
                friend.getRequesterId(),
                friend.getReceiverId(),
                friend.getStatus().name()
        );
    }
}