package hello.connectme.friend.controller;

import hello.connectme.friend.dto.FriendStatusResponse;
import hello.connectme.friend.dto.FriendSummaryResponse;
import hello.connectme.friend.service.FriendService;
import hello.connectme.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 친구 관계 REST 컨트롤러
 * 친구 목록/요청 조회, 친구 요청 전송/수락/거절/차단/삭제 엔드포인트 제공 (/friends/**)
 */
@Tag(name = "친구", description = "친구 목록/요청 조회, 친구 요청 전송/수락/거절/차단/삭제 API")
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 수락된 친구 목록 조회
     * @param userDetails 인증된 사용자 정보
     * @return 200 OK + 친구 목록
     */
    @Operation(summary = "친구 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendSummaryResponse>>> getFriends(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.getFriends(userId)));
    }

    /**
     * 수신한 친구 요청 목록 조회
     * @param userDetails 인증된 사용자 정보
     * @return 200 OK + 친구 요청 목록
     */
    @Operation(summary = "수신한 친구 요청 목록 조회")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendSummaryResponse>>> getFriendRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.getFriendRequests(userId)));
    }

    /**
     * 친구 요청 전송 — 성공 시 201 Created 반환
     * @param userDetails 인증된 사용자 정보
     * @param targetId 친구 요청을 받을 회원 ID
     * @return 201 Created + 생성된 친구 요청 상태
     */
    @Operation(summary = "친구 요청 전송")
    @PostMapping("/request/{targetId}")
    public ResponseEntity<ApiResponse<FriendStatusResponse>> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetId) {
        Long requesterId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(friendService.sendFriendRequest(requesterId, targetId)));
    }

    /**
     * 친구 요청 수락
     * @param userDetails 인증된 사용자 정보
     * @param friendId 수락할 친구 관계 ID
     * @return 200 OK + 수락된 친구 상태
     */
    @Operation(summary = "친구 요청 수락")
    @PatchMapping("/{friendId}/accept")
    public ResponseEntity<ApiResponse<FriendStatusResponse>> acceptFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.acceptFriend(friendId, userId)));
    }

    /**
     * 친구 요청 거절 — 204 No Content 반환
     * @param userDetails 인증된 사용자 정보
     * @param friendId 거절할 친구 관계 ID
     * @return 204 No Content
     */
    @Operation(summary = "친구 요청 거절")
    @PatchMapping("/{friendId}/reject")
    public ResponseEntity<Void> rejectFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.rejectFriend(friendId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 친구 차단
     * @param userDetails 인증된 사용자 정보
     * @param friendId 차단할 친구 관계 ID
     * @return 200 OK + 차단된 친구 상태
     */
    @Operation(summary = "친구 차단")
    @PatchMapping("/{friendId}/block")
    public ResponseEntity<ApiResponse<FriendStatusResponse>> blockFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.blockFriend(friendId, userId)));
    }

    /**
     * 친구 삭제 — 204 No Content 반환
     * @param userDetails 인증된 사용자 정보
     * @param friendId 삭제할 친구 관계 ID
     * @return 204 No Content
     */
    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.deleteFriend(friendId, userId);
        return ResponseEntity.noContent().build();
    }
}