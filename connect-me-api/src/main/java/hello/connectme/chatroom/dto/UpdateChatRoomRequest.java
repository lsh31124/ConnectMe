package hello.connectme.chatroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 채팅방 이름 수정 요청 DTO — OWNER 권한 보유자만 변경 가능
 */
public record UpdateChatRoomRequest(
        @NotBlank @Size(max = 100, message = "채팅방 이름은 100자 이하여야 합니다") String name
) {}