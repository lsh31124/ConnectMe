package hello.connectme.domain.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    Optional<ChatRoomMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);
    List<ChatRoomMember> findByUserIdAndLeftAtIsNull(Long userId);
}