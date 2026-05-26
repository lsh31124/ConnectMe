package hello.connectme.chatroom.dto;

import hello.connectme.domain.chatroom.ChatRoomMember;

import java.time.LocalDateTime;

/**
 * 채팅방 멤버 응답 DTO
 * 채팅방 상세 조회 시 멤버 목록에 포함되는 개별 멤버 정보
 */
public record ChatRoomMemberResponse(
        Long id,
        Long userId,
        String role,
        boolean isPinned,
        LocalDateTime joinedAt
) {
    /**
     * ChatRoomMember 엔티티를 ChatRoomMemberResponse로 변환
     * @param member 채팅방 멤버 엔티티
     * @return ChatRoomMemberResponse 인스턴스
     */
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