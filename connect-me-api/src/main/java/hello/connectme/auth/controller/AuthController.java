package hello.connectme.auth.controller;

import hello.connectme.auth.dto.LoginRequest;
import hello.connectme.auth.dto.RegisterRequest;
import hello.connectme.auth.dto.TokenResponse;
import hello.connectme.auth.service.AuthService;
import hello.connectme.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 REST 컨트롤러
 * 회원 가입, 로그인, 토큰 갱신, 로그아웃 엔드포인트 제공 (/auth/**)
 */
@Tag(name = "인증", description = "회원 가입, 로그인, 토큰 갱신, 로그아웃 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원 가입 — 성공 시 201 Created와 토큰 반환
     * @param request 회원 가입 요청 정보
     * @return 발급된 토큰 응답
     */
    @Operation(summary = "회원 가입", description = "이메일, 비밀번호, 전화번호로 회원 가입 후 JWT 토큰 반환")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request)));
    }

    /**
     * 로그인 — 성공 시 200 OK와 토큰 반환
     * @param request 로그인 요청 정보
     * @return 발급된 토큰 응답
     */
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인 후 JWT 액세스·리프레시 토큰 반환")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    /**
     * 토큰 갱신 — 리프레시 토큰으로 새 액세스 토큰 발급
     * @param body {"refreshToken": "..."} 형식의 요청 바디
     * @return 새로 발급된 토큰 응답
     */
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스 토큰 발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(body.get("refreshToken"))));
    }

    /**
     * 로그아웃 — 리프레시 토큰 무효화
     * @param body {"refreshToken": "..."} 형식의 요청 바디
     * @return 빈 성공 응답
     */
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃 처리")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        return ResponseEntity.ok(ApiResponse.ok());
    }
}