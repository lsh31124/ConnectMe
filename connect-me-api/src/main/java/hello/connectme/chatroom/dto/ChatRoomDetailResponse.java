package hello.connectme.chatroom.dto;

import hello.connectme.domain.chatroom.ChatRoom;

import java.util.List;

/**
 * 채팅방 상세 응답 DTO
 * 채팅방 단건 조회 시 핀 메시지 ID와 전체 멤버 목록을 포함한 상세 정보 반환
 */
public record ChatRoomDetailResponse(
        Long id,
        String name,
        String type,
        Long createdById,
        Long pinnedMessageId,
        List<ChatRoomMemberResponse> members
) {
    /**
     * ChatRoom 엔티티와 멤버 응답 목록을 ChatRoomDetailResponse로 변환
     * @param room 채팅방 엔티티
     * @param members 변환된 멤버 응답 목록
     * @return ChatRoomDetailResponse 인스턴스
     */
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