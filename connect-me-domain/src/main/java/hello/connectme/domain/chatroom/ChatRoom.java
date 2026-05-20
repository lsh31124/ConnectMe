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

    public static ChatRoom createDirect(Long createdById) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.type = ChatRoomType.DIRECT;
        chatRoom.createdById = createdById;
        return chatRoom;
    }

    public static ChatRoom createGroup(String name, Long createdById) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.name = name;
        chatRoom.type = ChatRoomType.GROUP;
        chatRoom.createdById = createdById;
        return chatRoom;
    }

    public void updateName(String name) {
        this.name = name;
    }
}