package hello.connectme.chatroom.dto;

import hello.connectme.domain.chatroom.ChatRoom;

import java.util.List;

public record ChatRoomDetailResponse(
        Long id,
        String name,
        String type,
        Long createdById,
        Long pinnedMessageId,
        List<ChatRoomMemberResponse> members
) {
    public static ChatRoomDetailResponse from(ChatRoom room, List<ChatRoomMemberResponse> members) {
        return new ChatRoomDetailResponse(
                room.getId(),
                room.getName(),
                room.getType().name(),
                room.getCreatedById(),
                room.getPinnedMessageId(),
                members
        );
    }
}