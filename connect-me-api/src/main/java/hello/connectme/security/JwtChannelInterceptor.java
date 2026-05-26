package hello.connectme.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * WebSocket STOMP 채널 인터셉터
 * CONNECT 커맨드 수신 시 JWT 토큰을 검증하고 세션 속성에 userId 저장
 * MessageController에서 headerAccessor.getSessionAttributes().get("userId")로 접근
 */
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * STOMP 메시지 전송 전 처리 — CONNECT 시 JWT 인증 수행
     * @param message 전송할 STOMP 메시지
     * @param channel 메시지 채널
     * @return 인증된 메시지 (인증 실패 시 MessagingException 발생)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            // Authorization 헤더 존재 여부 및 형식 확인
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing Authorization header");
            }
            String token = authHeader.substring(7);
            // JWT 유효성 검사
            if (!jwtTokenProvider.validateToken(token)) {
                throw new MessagingException("Invalid JWT token");
            }
            // 인증 성공 시 세션 속성에 userId 저장
            Long userId = jwtTokenProvider.getUserId(token);
            accessor.getSessionAttributes().put("userId", userId);
        }
        return message;
    }
}