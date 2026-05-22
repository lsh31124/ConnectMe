package hello.connectme.security;

import hello.connectme.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security의 UserDetailsService 구현체
 * 이메일 또는 회원 ID를 기반으로 UserDetails 객체 로드
 * username 필드에 userId(Long)를 문자열로 저장하여 컨트롤러에서 Long.parseLong()으로 추출
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 UserDetails 로드 (Spring Security 기본 인터페이스)
     * @param email 이메일 주소
     * @return UserDetails 객체 (username = userId)
     * @throws UsernameNotFoundException 이메일에 해당하는 회원이 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(String.valueOf(user.getId()))
                        .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /**
     * 회원 ID로 UserDetails 로드 — JWT 필터에서 토큰 파싱 후 호출
     * @param id 회원 ID
     * @return UserDetails 객체 (username = userId)
     * @throws UsernameNotFoundException 해당 ID의 회원이 없는 경우
     */
    public UserDetails loadUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(String.valueOf(user.getId()))
                        .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }
}