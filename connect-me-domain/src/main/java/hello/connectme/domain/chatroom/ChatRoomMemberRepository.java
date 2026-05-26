package hello.connectme.domain.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅방 멤버 데이터 접근 레포지토리
 * 채팅방별 멤버 조회 및 회원의 활성 채팅방 목록 조회 제공
 */
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    /**
     * 채팅방 ID와 회원 ID로 멤버 조회
     * @param chatRoomId 채팅방 ID
     * @param userId 회원 ID
     * @return 해당 멤버 (없으면 empty)
     */
    Optional<ChatRoomMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    Optional<ChatRoomMember> findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(Long chatRoomId, Long userId);

    /**
     * 채팅방에 속한 전체 멤버 목록 조회
     * @param chatRoomId 채팅방 ID
     * @return 멤버 목록
     */
    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);

    /**
     * 특정 회원이 현재 참여 중인(퇴장하지 않은) 채팅방 멤버십 목록 조회
     * @param userId 회원 ID
     * @return 퇴장하지 않은 멤버십 목록
     */
    List<ChatRoomMember> findByUserIdAndLeftAtIsNull(Long userId);

    /**
     * 여러 채팅방 ID에 속한 전체 멤버 목록 일괄 조회 (N+1 방지)
     * @param chatRoomIds 채팅방 ID 목록
     * @return 해당 채팅방들의 멤버 목록
     */
    List<ChatRoomMember> findByChatRoomIdIn(List<Long> chatRoomIds);
}