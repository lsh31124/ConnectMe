package hello.connectme.chatroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 그룹 채팅방 생성 요청 DTO
 * 채팅방 이름과 초대할 멤버 ID 목록을 포함
 */
public record CreateGroupRoomRequest(
        @NotBlank @Size(max = 100, message = "채팅방 이름은 100자 이하여야 합니다") String name,
        @NotNull @NotEmpty List<Long> memberIds
) {}