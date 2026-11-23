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

/**
 * 채팅방 엔티티
 * 1:1 다이렉트(DIRECT)와 그룹(GROUP) 두 가지 유형 지원
 * 핀 메시지 및 이름 변경 기능 포함
 */
@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChatRoomType type;

    @Column(nullable = false)
    private Long createdById;

    @Column
    private Long pinnedMessageId;

    @Column(unique = true, length = 30)
    private String directRoomKey;

    /**
     * 1:1 다이렉트 채팅방 생성 팩토리 메서드
     * directRoomKey = min(userId, otherUserId) + "_" + max(...) — UNIQUE 제약으로 중복 방 생성 방지
     * @param createdById 채팅방을 생성하는 회원 ID
     * @param otherUserId 상대방 회원 ID
     * @return 생성된 ChatRoom 엔티티
     */
    public static ChatRoom createDirect(Long createdById, Long otherUserId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.type = ChatRoomType.DIRECT;
        chatRoom.createdById = createdById;
        long min = Math.min(createdById, otherUserId);
        long max = Math.max(createdById, otherUserId);
        chatRoom.directRoomKey = min + "_" + max;
        return chatRoom;
    }

    /**
     * 그룹 채팅방 생성 팩토리 메서드
     * @param name 채팅방 이름
     * @param createdById 채팅방을 생성하는 회원 ID
     * @return 생성된 ChatRoom 엔티티
     */
    public static ChatRoom createGroup(String name, Long createdById) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.name = name;
        chatRoom.type = ChatRoomType.GROUP;
        chatRoom.createdById = createdById;
        return chatRoom;
    }

    /**
     * 채팅방 이름 변경
     * @param name 변경할 채팅방 이름
     */
    public void updateName(String name) {
        this.name = name;
    }

    public void pinMessage(Long messageId) {
        this.pinnedMessageId = messageId;
    }
}