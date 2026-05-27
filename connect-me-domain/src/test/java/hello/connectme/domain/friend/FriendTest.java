package hello.connectme.domain.friend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FriendTest {

    @Test
    void create_setsAllFields() {
        Friend friend = Friend.create(1L, 2L);

        assertThat(friend.getRequesterId()).isEqualTo(1L);
        assertThat(friend.getReceiverId()).isEqualTo(2L);
        assertThat(friend.getStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @Test
    void accept_changesPendingToAccepted() {
        Friend friend = Friend.create(1L, 2L);

        friend.accept();

        assertThat(friend.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
    }

    @Test
    void block_changesAcceptedToBlocked() {
        Friend friend = Friend.create(1L, 2L);
        friend.accept();

        friend.block(1L);

        assertThat(friend.getStatus()).isEqualTo(FriendStatus.BLOCKED);
        assertThat(friend.getBlockedById()).isEqualTo(1L);
    }

    @Test
    void accept_whenNotPending_throwsException() {
        Friend friend = Friend.create(1L, 2L);
        friend.accept();

        assertThatThrownBy(friend::accept)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void block_whenNotAccepted_throwsException() {
        Friend friend = Friend.create(1L, 2L);

        assertThatThrownBy(() -> friend.block(1L))
                .isInstanceOf(IllegalStateException.class);
    }
}