package hello.connectme.domain.user;

/**
 * 회원 인증 제공자 유형
 * LOCAL: 이메일/비밀번호 가입, GOOGLE/APPLE: 소셜 로그인
 */
public enum AuthProvider {
    LOCAL, GOOGLE, APPLE
}