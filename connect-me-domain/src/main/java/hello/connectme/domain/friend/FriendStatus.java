package hello.connectme.domain.friend;

/**
 * 친구 관계 상태
 * PENDING: 요청 대기 중, ACCEPTED: 친구 수락됨, BLOCKED: 차단됨
 */
public enum FriendStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}