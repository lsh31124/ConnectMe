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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 친구 비즈니스 로직 서비스
 * 친구 목록 조회, 친구 요청 전송/수락/거절, 차단, 삭제 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 수락된 친구 목록 조회 — 요청자/수신자 양방향 모두 포함
     * @param userId 조회할 회원 ID
     * @return 친구 요약 정보 목록
     */
    public List<FriendSummaryResponse> getFriends(Long userId) {
        List<Friend> friends = friendRepository.findAcceptedFriends(userId);
        List<Long> partnerIds = friends.stream()
                .map(f -> f.getRequesterId().equals(userId) ? f.getReceiverId() : f.getRequesterId())
                .toList();
        Map<Long, User> userMap = userRepository.findAllById(partnerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return friends.stream()
                .map(friend -> {
                    Long partnerId = friend.getRequesterId().equals(userId)
                            ? friend.getReceiverId()
                            : friend.getRequesterId();
                    User partner = userMap.get(partnerId);
                    if (partner == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                    return FriendSummaryResponse.from(friend, partner);
                })
                .toList();
    }

    /**
     * 수신한 친구 요청 목록 조회 (PENDING 상태만)
     * @param userId 수신자 회원 ID
     * @return 친구 요청 목록
     */
    public List<FriendSummaryResponse> getFriendRequests(Long userId) {
        List<Friend> requests = friendRepository.findByReceiverIdAndStatus(userId, FriendStatus.PENDING);
        List<Long> requesterIds = requests.stream().map(Friend::getRequesterId).toList();
        Map<Long, User> userMap = userRepository.findAllById(requesterIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return requests.stream()
                .map(friend -> {
                    User requester = userMap.get(friend.getRequesterId());
                    if (requester == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                    return FriendSummaryResponse.from(friend, requester);
                })
                .toList();
    }

    /**
     * 친구 요청 전송 — 자기 자신, 존재하지 않는 회원, 중복 요청 검사 포함
     * @param requesterId 요청을 보내는 회원 ID
     * @param targetId 요청을 받을 회원 ID
     * @return 생성된 친구 요청 상태
     */
    @Transactional
    public FriendStatusResponse sendFriendRequest(Long requesterId, Long targetId) {
        // 자기 자신에게 친구 요청 방지
        if (requesterId.equals(targetId)) {
            throw new BusinessException(ErrorCode.FRIEND_SELF_REQUEST);
        }
        if (!userRepository.existsById(targetId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 이미 친구 관계(방향 무관)가 존재하는 경우 중복 요청 방지
        if (friendRepository.existsBetween(requesterId, targetId)) {
            throw new BusinessException(ErrorCode.FRIEND_ALREADY_EXISTS);
        }
        Friend friend = friendRepository.save(Friend.create(requesterId, targetId));
        return FriendStatusResponse.from(friend);
    }

    /**
     * 친구 요청 수락 — 수신자만 수락 가능
     * @param friendId 친구 관계 ID
     * @param receiverId 수락하는 회원 ID (수신자여야 함)
     * @return 수락된 친구 상태
     */
    @Transactional
    public FriendStatusResponse acceptFriend(Long friendId, Long receiverId) {
        Friend friend = findFriend(friendId);
        // 수신자 본인만 수락 가능
        if (!friend.getReceiverId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friend.accept();
        return FriendStatusResponse.from(friend);
    }

    /**
     * 친구 요청 거절 — 수신자만 거절 가능하며 관계 삭제
     * @param friendId 친구 관계 ID
     * @param receiverId 거절하는 회원 ID (수신자여야 함)
     */
    @Transactional
    public void rejectFriend(Long friendId, Long receiverId) {
        Friend friend = findFriend(friendId);
        // 수신자 본인만 거절 가능
        if (!friend.getReceiverId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friendRepository.delete(friend);
    }

    /**
     * 친구 차단 — 요청자 또는 수신자 모두 가능
     * @param friendId 친구 관계 ID
     * @param userId 차단하는 회원 ID
     * @return 차단된 친구 상태
     */
    @Transactional
    public FriendStatusResponse blockFriend(Long friendId, Long userId) {
        Friend friend = findFriend(friendId);
        // 친구 관계의 구성원만 차단 가능
        if (!friend.getRequesterId().equals(userId) && !friend.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friend.block(userId);
        return FriendStatusResponse.from(friend);
    }

    /**
     * 친구 삭제 — 요청자 또는 수신자 모두 가능
     * @param friendId 친구 관계 ID
     * @param userId 삭제를 요청하는 회원 ID
     */
    @Transactional
    public void deleteFriend(Long friendId, Long userId) {
        Friend friend = findFriend(friendId);
        // 친구 관계의 구성원만 삭제 가능
        if (!friend.getRequesterId().equals(userId) && !friend.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_INVALID_STATUS);
        }
        friendRepository.delete(friend);
    }

    /**
     * 친구 관계 ID로 Friend 엔티티 조회 — 없으면 예외 발생
     * @param friendId 친구 관계 ID
     * @return Friend 엔티티
     */
    private Friend findFriend(Long friendId) {
        return friendRepository.findById(friendId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_NOT_FOUND));
    }
}