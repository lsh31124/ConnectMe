package hello.connectme.domain.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 채팅 메시지 데이터 접근 레포지토리
 * 커서 기반 페이지네이션을 통한 메시지 목록 조회 제공
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 채팅방의 최신 메시지 목록을 내림차순으로 조회 (첫 페이지)
     * @param chatRoomId 채팅방 ID
     * @param pageable 페이지 크기 설정
     * @return 최신순 정렬된 메시지 목록
     */
    List<Message> findByChatRoomIdOrderByIdDesc(Long chatRoomId, Pageable pageable);

    /**
     * 특정 커서 ID보다 오래된 메시지 목록 조회 (다음 페이지)
     * @param chatRoomId 채팅방 ID
     * @param cursorId 마지막으로 조회한 메시지 ID (이 ID보다 작은 메시지 조회)
     * @param pageable 페이지 크기 설정
     * @return 커서 이전의 메시지 목록
     */
    List<Message> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, Long cursorId, Pageable pageable);
}