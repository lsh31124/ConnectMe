package hello.connectme.chatroom.dto;

import hello.connectme.domain.chatroom.ChatRoom;

import java.util.List;

/**
 * 채팅방 요약 응답 DTO
 * 채팅방 목록 조회 시 사용되며 멤버 수를 포함한 기본 정보 제공
 */
public record ChatRoomResponse(
        Long id,
        String name,
        String type,
        Long createdById,
        int memberCount
) {
    /**
     * ChatRoom 엔티티와 멤버 목록을 ChatRoomResponse로 변환
     * @param room 채팅방 엔티티
     * @param members 채팅방 멤버 목록 (size만 사용)
     * @return ChatRoomResponse 인스턴스
     */
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