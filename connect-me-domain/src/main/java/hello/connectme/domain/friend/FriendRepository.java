package hello.connectme.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    List<Friend> findByReceiverIdAndStatus(Long receiverId, FriendStatus status);

    boolean existsByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    @Query("SELECT f FROM Friend f WHERE (f.requesterId = :userId OR f.receiverId = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);
}