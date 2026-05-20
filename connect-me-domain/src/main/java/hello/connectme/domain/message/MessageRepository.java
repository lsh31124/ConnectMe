package hello.connectme.domain.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatRoomIdOrderByIdDesc(Long chatRoomId, Pageable pageable);

    List<Message> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, Long cursorId, Pageable pageable);
}