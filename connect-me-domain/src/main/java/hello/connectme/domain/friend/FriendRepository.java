package hello.connectme.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 데이터 접근 레포지토리
 * 양방향 친구 관계 조회 및 중복 검사 기능 제공
 */
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * 요청자와 수신자 ID로 친구 관계 조회
     * @param requesterId 요청자 ID
     * @param receiverId 수신자 ID
     * @return 친구 관계 (없으면 empty)
     */
    Optional<Friend> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    /**
     * 특정 상태의 친구 요청 수신 목록 조회
     * @param receiverId 수신자 ID
     * @param status 조회할 친구 상태
     * @return 해당 상태의 친구 요청 목록
     */
    List<Friend> findByReceiverIdAndStatus(Long receiverId, FriendStatus status);

    /**
     * 두 회원 간 친구 관계(방향 무관) 존재 여부 확인
     * @param userId1 첫 번째 회원 ID
     * @param userId2 두 번째 회원 ID
     * @return 관계가 존재하면 true
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE (f.requesterId = :a AND f.receiverId = :b) OR (f.requesterId = :b AND f.receiverId = :a)")
    boolean existsBetween(@Param("a") Long userId1, @Param("b") Long userId2);

    /**
     * 특정 회원의 수락된 친구 목록 조회 (요청자 또는 수신자 모두 포함)
     * @param userId 조회 대상 회원 ID
     * @return ACCEPTED 상태의 친구 관계 목록
     */
    @Query("SELECT f FROM Friend f WHERE (f.requesterId = :userId OR f.receiverId = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);
}