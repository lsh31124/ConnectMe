package hello.connectme.user.controller;

import hello.connectme.global.response.ApiResponse;
import hello.connectme.user.dto.UpdateProfileRequest;
import hello.connectme.user.dto.UserProfileResponse;
import hello.connectme.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 회원 REST 컨트롤러
 * 내 프로필 조회/수정, 회원 검색, 회원 탈퇴 엔드포인트 제공 (/users/**)
 */
@Tag(name = "회원", description = "내 프로필 조회/수정, 회원 검색, 회원 탈퇴 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     * @param userDetails JWT 인증에서 추출된 사용자 정보
     * @return 200 OK + 프로필 응답
     */
    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    /**
     * 내 프로필 수정
     * @param userDetails JWT 인증에서 추출된 사용자 정보
     * @param request 수정할 프로필 정보
     * @return 200 OK + 수정된 프로필 응답
     */
    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(userId, request)));
    }

    /**
     * 이메일 또는 전화번호로 회원 검색
     * @param query 검색할 이메일 또는 전화번호
     * @return 200 OK + 회원 목록
     */
    @Operation(summary = "회원 검색", description = "이메일 또는 전화번호로 회원 검색")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(userService.searchUsers(query)));
    }

    /**
     * 회원 탈퇴
     * @param userDetails JWT 인증에서 추출된 사용자 정보
     * @return 204 No Content
     */
    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}