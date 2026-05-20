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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public List<FriendSummaryResponse> getFriends(Long userId) {
        return friendRepository.findAcceptedFriends(userId).stream()
                .map(friend -> {
                    Long partnerId = friend.getRequesterId().equals(userId)
                            ? friend.getReceiverId()
                            : friend.getRequesterId();
                    User partner = userRepository.findById(partnerId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    return FriendSummaryResponse.from(friend, partner);
                })
                .toList();
    }

    public List<FriendSummaryResponse> getFriendRequests(Long userId) {
        return friendRepository.findByReceiverIdAndStatus(userId, FriendStatus.PENDING).stream()
                .map(friend -> {
                    User requester = userRepository.findById(friend.getRequesterId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    return FriendSummaryResponse.from(friend, requester);
                })
                .toList();
    }

    @Transactional
    public FriendStatusResponse sendFriendRequest(Long requesterId, Long targetId) {
        if (!userRepository.existsById(targetId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (friendRepository.existsByRequesterIdAndReceiverId(requesterId, targetId)
                || friendRepository.existsByRequesterIdAndReceiverId(targetId, requesterId)) {
            throw new BusinessException(ErrorCode.FRIEND_ALREADY_EXISTS);
        }
        Friend friend = friendRepository.save(Friend.create(requesterId, targetId));
        return FriendStatusResponse.from(friend);
    }

    @Transactional
    public FriendStatusResponse acceptFriend(Long friendId, Long receiverId) {
        Friend friend = findFriend(friendId);
        if (!friend.getReceiverId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friend.accept();
        return FriendStatusResponse.from(friend);
    }

    @Transactional
    public void rejectFriend(Long friendId, Long receiverId) {
        Friend friend = findFriend(friendId);
        if (!friend.getReceiverId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friendRepository.delete(friend);
    }

    @Transactional
    public FriendStatusResponse blockFriend(Long friendId, Long userId) {
        Friend friend = findFriend(friendId);
        if (!friend.getRequesterId().equals(userId) && !friend.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friend.block();
        return FriendStatusResponse.from(friend);
    }

    @Transactional
    public void deleteFriend(Long friendId, Long userId) {
        Friend friend = findFriend(friendId);
        if (!friend.getRequesterId().equals(userId) && !friend.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friendRepository.delete(friend);
    }

    private Friend findFriend(Long friendId) {
        return friendRepository.findById(friendId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_NOT_FOUND));
    }
}