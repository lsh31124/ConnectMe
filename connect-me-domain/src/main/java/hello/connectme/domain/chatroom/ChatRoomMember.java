package hello.connectme.domain.chatroom;

import hello.connectme.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 멤버 엔티티
 * 채팅방과 회원의 다대다 관계를 해소하는 연결 엔티티
 * 역할(OWNER/MEMBER), 핀 여부, 입장/퇴장 시각 관리
 */
@Entity
@Table(name = "chat_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChatRoomMemberRole role;

    @Column(nullable = false)
    private boolean isPinned = false;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime leftAt;

    /**
     * 채팅방 멤버 입장 팩토리 메서드 — 입장 시각 자동 설정
     * @param chatRoomId 입장할 채팅방 ID
     * @param userId 입장하는 회원 ID
     * @param role 부여할 역할 (OWNER 또는 MEMBER)
     * @return 생성된 ChatRoomMember 엔티티
     */
    public static ChatRoomMember join(Long chatRoomId, Long userId, ChatRoomMemberRole role) {
        ChatRoomMember member = new ChatRoomMember();
        member.chatRoomId = chatRoomId;
        member.userId = userId;
        member.role = role;
        member.isPinned = false;
        member.joinedAt = LocalDateTime.now();
        return member;
    }

    /**
     * 채팅방 퇴장 처리 — leftAt에 현재 시각 기록
     */
    public void leave() {
        this.leftAt = LocalDateTime.now();
    }
}