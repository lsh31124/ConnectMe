package hello.connectme.chatroom.dto;

/**
 * 1:1 다이렉트 채팅방 생성 요청 DTO
 */
public record CreateDirectRoomRequest(Long targetUserId) {}