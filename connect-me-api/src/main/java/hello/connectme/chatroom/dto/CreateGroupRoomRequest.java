package hello.connectme.chatroom.dto;

import java.util.List;

public record CreateGroupRoomRequest(String name, List<Long> memberIds) {}