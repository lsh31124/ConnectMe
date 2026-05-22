package hello.connectme.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 액세스 토큰 및 리프레시 토큰 생성/검증 컴포넌트
 * HMAC-SHA 알고리즘으로 토큰에 서명하며 userId를 subject로 저장
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    /**
     * userId를 subject로 포함하는 JWT 액세스 토큰 생성
     * @param userId 토큰에 포함할 회원 ID
     * @return 서명된 JWT 토큰 문자열
     */
    public String createAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * UUID 기반 리프레시 토큰 생성 (DB에 저장하여 관리)
     * @return 랜덤 UUID 문자열
     */
    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * JWT 토큰에서 userId 추출
     * @param token JWT 토큰 문자열
     * @return 회원 ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }

    /**
     * JWT 토큰의 유효성 검사 (서명 및 만료 시각)
     * @param token 검사할 JWT 토큰 문자열
     * @return 유효하면 true, 만료/위변조 시 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Base64 인코딩된 시크릿 키를 HMAC-SHA 서명 키로 변환
     * @return SecretKey 인스턴스
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}