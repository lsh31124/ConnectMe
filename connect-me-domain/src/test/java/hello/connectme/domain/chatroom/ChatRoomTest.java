package hello.connectme.domain.chatroom;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomTest {

    @Test
    void createDirect_setsTypeAndCreatedById() {
        ChatRoom chatRoom = ChatRoom.createDirect(1L, 2L);

        assertThat(chatRoom.getType()).isEqualTo(ChatRoomType.DIRECT);
        assertThat(chatRoom.getCreatedById()).isEqualTo(1L);
        assertThat(chatRoom.getDirectRoomKey()).isEqualTo("1_2");
    }

    @Test
    void createDirect_hasNullName() {
        ChatRoom chatRoom = ChatRoom.createDirect(1L, 2L);

        assertThat(chatRoom.getName()).isNull();
    }

    @Test
    void createDirect_hasnullPinnedMessageId() {
        ChatRoom chatRoom = ChatRoom.createDirect(1L, 2L);

        assertThat(chatRoom.getPinnedMessageId()).isNull();
    }

    @Test
    void createGroup_setsAllFields() {
        ChatRoom chatRoom = ChatRoom.createGroup("개발팀 채팅방", 2L);

        assertThat(chatRoom.getName()).isEqualTo("개발팀 채팅방");
        assertThat(chatRoom.getType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(chatRoom.getCreatedById()).isEqualTo(2L);
        assertThat(chatRoom.getPinnedMessageId()).isNull();
    }
}