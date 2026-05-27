package hello.connectme.domain.friend;

import hello.connectme.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 관계 엔티티
 * 요청자(requester)와 수신자(receiver) 쌍으로 구성되며 상태(PENDING/ACCEPTED/BLOCKED) 관리
 * requester_id + receiver_id 조합은 유니크 제약으로 중복 요청 방지
 */
@Entity
@Table(
    name = "friends",
    uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "receiver_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FriendStatus status;

    @Column(name = "blocked_by_id")
    private Long blockedById;

    /**
     * 친구 요청 생성 팩토리 메서드 — 초기 상태는 PENDING
     * @param requesterId 친구 요청을 보내는 회원 ID
     * @param receiverId 친구 요청을 받는 회원 ID
     * @return 생성된 Friend 엔티티
     */
    public static Friend create(Long requesterId, Long receiverId) {
        Friend friend = new Friend();
        friend.requesterId = requesterId;
        friend.receiverId = receiverId;
        friend.status = FriendStatus.PENDING;
        return friend;
    }

    /**
     * 친구 요청 수락 — PENDING 상태에서만 가능
     */
    public void accept() {
        // PENDING 상태가 아니면 수락 불가
        if (this.status != FriendStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 수락할 수 있습니다.");
        }
        this.status = FriendStatus.ACCEPTED;
    }

    /**
     * 친구 차단 — ACCEPTED 상태에서만 가능, 차단한 사용자를 blockedById에 기록
     */
    public void block(Long blockerId) {
        if (this.status != FriendStatus.ACCEPTED) {
            throw new IllegalStateException("ACCEPTED 상태에서만 차단할 수 있습니다.");
        }
        this.status = FriendStatus.BLOCKED;
        this.blockedById = blockerId;
    }
}