package hello.connectme.chatroom.controller;

import hello.connectme.chatroom.dto.ChatRoomDetailResponse;
import hello.connectme.chatroom.dto.ChatRoomResponse;
import hello.connectme.chatroom.dto.CreateDirectRoomRequest;
import hello.connectme.chatroom.dto.CreateGroupRoomRequest;
import hello.connectme.chatroom.dto.InviteMemberRequest;
import hello.connectme.chatroom.dto.UpdateChatRoomRequest;
import hello.connectme.chatroom.service.ChatRoomService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createDirectRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateDirectRoomRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatRoomService.createDirectRoom(userId, request.targetUserId())));
    }

    @PostMapping("/group")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createGroupRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateGroupRoomRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatRoomService.createGroupRoom(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyChatRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(chatRoomService.getMyChatRooms(userId)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoomDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(chatRoomService.getChatRoomDetail(userId, roomId)));
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> updateChatRoomName(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody UpdateChatRoomRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(chatRoomService.updateChatRoomName(userId, roomId, request)));
    }

    @PostMapping("/{roomId}/members")
    public ResponseEntity<Void> inviteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody InviteMemberRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatRoomService.inviteMember(userId, roomId, request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{roomId}/members/me")
    public ResponseEntity<Void> leaveChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatRoomService.leaveChatRoom(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roomId}/members/{targetUserId}")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatRoomService.kickMember(userId, roomId, targetUserId);
        return ResponseEntity.noContent().build();
    }
}