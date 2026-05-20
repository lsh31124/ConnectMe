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

    public static ChatRoomMember join(Long chatRoomId, Long userId, ChatRoomMemberRole role) {
        ChatRoomMember member = new ChatRoomMember();
        member.chatRoomId = chatRoomId;
        member.userId = userId;
        member.role = role;
        member.isPinned = false;
        member.joinedAt = LocalDateTime.now();
        return member;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }
}