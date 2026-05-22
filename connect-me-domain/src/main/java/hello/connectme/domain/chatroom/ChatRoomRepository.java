package hello.connectme.domain.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 채팅방 데이터 접근 레포지토리
 * 기본 CRUD는 JpaRepository에서 제공
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}