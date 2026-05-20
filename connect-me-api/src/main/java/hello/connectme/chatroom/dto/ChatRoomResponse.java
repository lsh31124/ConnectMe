package hello.connectme.chatroom.dto;

import hello.connectme.domain.chatroom.ChatRoom;

import java.util.List;

public record ChatRoomResponse(
        Long id,
        String name,
        String type,
        Long createdById,
        int memberCount
) {
    public static ChatRoomResponse from(ChatRoom room, List<?> members) {
        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getType().name(),
                room.getCreatedById(),
                members.size()
        );
    }
}