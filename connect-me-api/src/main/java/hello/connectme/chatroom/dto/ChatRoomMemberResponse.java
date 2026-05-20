package hello.connectme.chatroom.dto;

import hello.connectme.domain.chatroom.ChatRoomMember;

import java.time.LocalDateTime;

public record ChatRoomMemberResponse(
        Long id,
        Long userId,
        String role,
        boolean isPinned,
        LocalDateTime joinedAt
) {
    public static ChatRoomMemberResponse from(ChatRoomMember member) {
        return new ChatRoomMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getRole().name(),
                member.isPinned(),
                member.getJoinedAt()
        );
    }
}