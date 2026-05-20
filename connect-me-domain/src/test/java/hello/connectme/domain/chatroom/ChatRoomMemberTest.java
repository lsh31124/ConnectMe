package hello.connectme.domain.chatroom;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomMemberTest {

    @Test
    void join_setsAllFields() {
        ChatRoomMember member = ChatRoomMember.join(10L, 20L, ChatRoomMemberRole.OWNER);

        assertThat(member.getChatRoomId()).isEqualTo(10L);
        assertThat(member.getUserId()).isEqualTo(20L);
        assertThat(member.getRole()).isEqualTo(ChatRoomMemberRole.OWNER);
    }

    @Test
    void join_isPinnedFalseByDefault() {
        ChatRoomMember member = ChatRoomMember.join(10L, 20L, ChatRoomMemberRole.MEMBER);

        assertThat(member.isPinned()).isFalse();
    }

    @Test
    void join_leftAtIsNullByDefault() {
        ChatRoomMember member = ChatRoomMember.join(10L, 20L, ChatRoomMemberRole.MEMBER);

        assertThat(member.getLeftAt()).isNull();
    }

    @Test
    void join_joinedAtIsSetOnCreation() {
        ChatRoomMember member = ChatRoomMember.join(10L, 20L, ChatRoomMemberRole.MEMBER);

        assertThat(member.getJoinedAt()).isNotNull();
    }

    @Test
    void leave_setsLeftAt() {
        ChatRoomMember member = ChatRoomMember.join(10L, 20L, ChatRoomMemberRole.MEMBER);

        member.leave();

        assertThat(member.getLeftAt()).isNotNull();
    }
}