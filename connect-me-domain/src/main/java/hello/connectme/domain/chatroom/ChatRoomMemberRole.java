package hello.connectme.domain.chatroom;

/**
 * 채팅방 멤버 역할
 * OWNER: 채팅방 생성자 (이름 변경, 멤버 강퇴 권한 보유), MEMBER: 일반 참여자
 */
public enum ChatRoomMemberRole {
    OWNER,
    MEMBER
}