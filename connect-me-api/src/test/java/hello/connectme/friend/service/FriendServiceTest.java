package hello.connectme.friend.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.friend.Friend;
import hello.connectme.domain.friend.FriendRepository;
import hello.connectme.domain.friend.FriendStatus;
import hello.connectme.domain.user.User;
import hello.connectme.domain.user.UserRepository;
import hello.connectme.friend.dto.FriendStatusResponse;
import hello.connectme.friend.dto.FriendSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @InjectMocks
    private FriendService friendService;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    private User createUser(Long id, String email, String name) {
        User user = User.createLocal(email, null, name, "pw");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Friend createFriend(Long id, Long requesterId, Long receiverId, FriendStatus status) {
        Friend friend = Friend.create(requesterId, receiverId);
        ReflectionTestUtils.setField(friend, "id", id);
        if (status == FriendStatus.ACCEPTED) ReflectionTestUtils.setField(friend, "status", FriendStatus.ACCEPTED);
        if (status == FriendStatus.BLOCKED) {
            ReflectionTestUtils.setField(friend, "status", FriendStatus.ACCEPTED);
            friend.block();
        }
        return friend;
    }

    @Test
    void sendFriendRequest_success_returnsPendingFriend() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(friendRepository.existsBetween(1L, 2L)).willReturn(false);
        Friend saved = Friend.create(1L, 2L);
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(friendRepository.save(any(Friend.class))).willReturn(saved);

        FriendStatusResponse response = friendService.sendFriendRequest(1L, 2L);

        assertThat(response.requesterId()).isEqualTo(1L);
        assertThat(response.receiverId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void sendFriendRequest_selfRequest_throwsException() {
        assertThatThrownBy(() -> friendService.sendFriendRequest(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FRIEND_SELF_REQUEST);
    }

    @Test
    void sendFriendRequest_targetNotFound_throwsException() {
        given(userRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> friendService.sendFriendRequest(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void sendFriendRequest_alreadyExists_throwsException() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(friendRepository.existsBetween(1L, 2L)).willReturn(true);

        assertThatThrownBy(() -> friendService.sendFriendRequest(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FRIEND_ALREADY_EXISTS);
    }

    @Test
    void sendFriendRequest_reverseAlreadyExists_throwsException() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(friendRepository.existsBetween(2L, 1L)).willReturn(true);

        assertThatThrownBy(() -> friendService.sendFriendRequest(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FRIEND_ALREADY_EXISTS);
    }

    @Test
    void acceptFriend_success_returnsAcceptedFriend() {
        Friend friend = createFriend(5L, 2L, 1L, FriendStatus.PENDING);
        given(friendRepository.findById(5L)).willReturn(Optional.of(friend));

        FriendStatusResponse response = friendService.acceptFriend(5L, 1L);

        assertThat(response.status()).isEqualTo("ACCEPTED");
    }

    @Test
    void acceptFriend_notReceiver_throwsException() {
        Friend friend = createFriend(5L, 2L, 1L, FriendStatus.PENDING);
        given(friendRepository.findById(5L)).willReturn(Optional.of(friend));

        assertThatThrownBy(() -> friendService.acceptFriend(5L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FRIEND_INVALID_STATUS);
    }

    @Test
    void rejectFriend_success_deletesFriend() {
        Friend friend = createFriend(5L, 2L, 1L, FriendStatus.PENDING);
        given(friendRepository.findById(5L)).willReturn(Optional.of(friend));

        friendService.rejectFriend(5L, 1L);

        then(friendRepository).should().delete(friend);
    }

    @Test
    void blockFriend_success_returnsBlockedFriend() {
        Friend friend = createFriend(5L, 1L, 2L, FriendStatus.ACCEPTED);
        given(friendRepository.findById(5L)).willReturn(Optional.of(friend));

        FriendStatusResponse response = friendService.blockFriend(5L, 1L);

        assertThat(response.status()).isEqualTo("BLOCKED");
    }

    @Test
    void deleteFriend_success() {
        Friend friend = createFriend(5L, 1L, 2L, FriendStatus.ACCEPTED);
        given(friendRepository.findById(5L)).willReturn(Optional.of(friend));

        friendService.deleteFriend(5L, 1L);

        then(friendRepository).should().delete(friend);
    }

    @Test
    void getFriends_returnsEnrichedList() {
        Friend friend = createFriend(1L, 1L, 2L, FriendStatus.ACCEPTED);
        User partner = createUser(2L, "b@email.com", "김철수");
        given(friendRepository.findAcceptedFriends(1L)).willReturn(List.of(friend));
        given(userRepository.findById(2L)).willReturn(Optional.of(partner));

        List<FriendSummaryResponse> result = friendService.getFriends(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("김철수");
        assertThat(result.get(0).email()).isEqualTo("b@email.com");
    }

    @Test
    void getFriendRequests_returnsPendingList() {
        Friend request = createFriend(3L, 4L, 1L, FriendStatus.PENDING);
        User requester = createUser(4L, "req@email.com", "박지수");
        given(friendRepository.findByReceiverIdAndStatus(1L, FriendStatus.PENDING)).willReturn(List.of(request));
        given(userRepository.findById(4L)).willReturn(Optional.of(requester));

        List<FriendSummaryResponse> result = friendService.getFriendRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("박지수");
    }
}