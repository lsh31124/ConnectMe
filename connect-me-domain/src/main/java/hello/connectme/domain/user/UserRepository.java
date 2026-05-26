package hello.connectme.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 회원 데이터 접근 레포지토리
 * 이메일, 전화번호, 소셜 제공자 기반 조회 및 중복 검사 제공
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 회원 조회
     * @param email 이메일 주소
     * @return 일치하는 회원 (없으면 empty)
     */
    Optional<User> findByEmail(String email);

    /**
     * 전화번호로 회원 조회
     * @param phoneNumber 전화번호
     * @return 일치하는 회원 (없으면 empty)
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 소셜 제공자와 제공자 ID로 회원 조회
     * @param provider 소셜 인증 제공자
     * @param providerId 소셜 제공자 고유 ID
     * @return 일치하는 회원 (없으면 empty)
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    /**
     * 이메일 중복 여부 확인
     * @param email 이메일 주소
     * @return 중복이면 true
     */
    boolean existsByEmail(String email);

    /**
     * 전화번호 중복 여부 확인
     * @param phoneNumber 전화번호
     * @return 중복이면 true
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 이메일 또는 전화번호로 회원 목록 검색
     * @param query 검색할 이메일 또는 전화번호
     * @return 일치하는 회원 목록
     */
    @Query("SELECT u FROM User u WHERE u.email = :query OR u.phoneNumber = :query")
    List<User> findByEmailOrPhoneNumber(@Param("query") String query);
}