package hello.connectme.chatroom.dto;

import java.util.List;

/**
 * 그룹 채팅방 생성 요청 DTO
 * 채팅방 이름과 초대할 멤버 ID 목록을 포함
 */
public record CreateGroupRoomRequest(String name, List<Long> memberIds) {}