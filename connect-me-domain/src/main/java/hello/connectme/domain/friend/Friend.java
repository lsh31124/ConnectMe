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

    public static Friend create(Long requesterId, Long receiverId) {
        Friend friend = new Friend();
        friend.requesterId = requesterId;
        friend.receiverId = receiverId;
        friend.status = FriendStatus.PENDING;
        return friend;
    }

    public void accept() {
        if (this.status != FriendStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 수락할 수 있습니다.");
        }
        this.status = FriendStatus.ACCEPTED;
    }

    public void block() {
        if (this.status != FriendStatus.ACCEPTED) {
            throw new IllegalStateException("ACCEPTED 상태에서만 차단할 수 있습니다.");
        }
        this.status = FriendStatus.BLOCKED;
    }
}