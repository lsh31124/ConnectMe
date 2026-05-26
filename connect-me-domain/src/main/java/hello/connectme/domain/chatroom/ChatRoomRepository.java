package hello.connectme.domain.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 채팅방 데이터 접근 레포지토리
 * 기본 CRUD는 JpaRepository에서 제공
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 두 유저 간 이미 존재하는 DIRECT 채팅방 조회 (멱등성 보장)
     * 양방향 모두 검색: userId1이 요청자든 수신자든 동일하게 찾음
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = 'DIRECT' AND " +
           "EXISTS (SELECT crm1 FROM ChatRoomMember crm1 WHERE crm1.chatRoomId = cr.id AND crm1.userId = :userId1 AND crm1.leftAt IS NULL) AND " +
           "EXISTS (SELECT crm2 FROM ChatRoomMember crm2 WHERE crm2.chatRoomId = cr.id AND crm2.userId = :userId2 AND crm2.leftAt IS NULL)")
    Optional<ChatRoom> findExistingDirectRoom(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}