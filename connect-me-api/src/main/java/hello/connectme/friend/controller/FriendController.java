package hello.connectme.friend.controller;

import hello.connectme.friend.dto.FriendStatusResponse;
import hello.connectme.friend.dto.FriendSummaryResponse;
import hello.connectme.friend.service.FriendService;
import hello.connectme.global.response.ApiResponse;
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

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendSummaryResponse>>> getFriends(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.getFriends(userId)));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendSummaryResponse>>> getFriendRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.getFriendRequests(userId)));
    }

    @PostMapping("/request/{targetId}")
    public ResponseEntity<ApiResponse<FriendStatusResponse>> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetId) {
        Long requesterId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(friendService.sendFriendRequest(requesterId, targetId)));
    }

    @PatchMapping("/{friendId}/accept")
    public ResponseEntity<ApiResponse<FriendStatusResponse>> acceptFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.acceptFriend(friendId, userId)));
    }

    @PatchMapping("/{friendId}/reject")
    public ResponseEntity<Void> rejectFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.rejectFriend(friendId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{friendId}/block")
    public ResponseEntity<ApiResponse<FriendStatusResponse>> blockFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(friendService.blockFriend(friendId, userId)));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.deleteFriend(friendId, userId);
        return ResponseEntity.noContent().build();
    }
}