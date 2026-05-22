package hello.connectme.chatroom.dto;

/**
 * 채팅방 이름 수정 요청 DTO — OWNER 권한 보유자만 변경 가능
 */
public record UpdateChatRoomRequest(String name) {}