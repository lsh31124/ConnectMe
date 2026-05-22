package hello.connectme.chatroom.controller;

import hello.connectme.chatroom.dto.ChatRoomDetailResponse;
import hello.connectme.chatroom.dto.ChatRoomResponse;
import hello.connectme.chatroom.dto.CreateDirectRoomRequest;
import hello.connectme.chatroom.dto.CreateGroupRoomRequest;
import hello.connectme.chatroom.dto.InviteMemberRequest;
import hello.connectme.chatroom.dto.UpdateChatRoomRequest;
import hello.connectme.chatroom.service.ChatRoomService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 채팅방 REST 컨트롤러
 * 채팅방 생성/조회/수정, 멤버 초대/퇴장/강퇴 엔드포인트 제공 (/chat-rooms/**)
 */
@Tag(name = "채팅방", description = "채팅방 생성/조회/수정, 멤버 초대/퇴장/강퇴 API")
@RestController
@RequestMapping("/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 1:1 다이렉트 채팅방 생성 — 201 Created
     * @param userDetails 인증된 사용자 정보
     * @param request 상대방 회원 ID 포함 요청
     * @return 201 Created + 생성된 채팅방 응답
     */
    @Operation(summary = "1:1 채팅방 생성")
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createDirectRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateDirectRoomRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatRoomService.createDirectRoom(userId, request.targetUserId())));
    }

    /**
     * 그룹 채팅방 생성 — 201 Created
     * @param userDetails 인증된 사용자 정보
     * @param request 채팅방 이름과 초대 멤버 목록 포함 요청
     * @return 201 Created + 생성된 채팅방 응답
     */
    @Operation(summary = "그룹 채팅방 생성")
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createGroupRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateGroupRoomRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatRoomService.createGroupRoom(userId, request)));
    }

    /**
     * 내 채팅방 목록 조회
     * @param userDetails 인증된 사용자 정보
     * @return 200 OK + 채팅방 목록
     */
    @Operation(summary = "내 채팅방 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyChatRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(chatRoomService.getMyChatRooms(userId)));
    }

    /**
     * 채팅방 상세 조회
     * @param userDetails 인증된 사용자 정보
     * @param roomId 조회할 채팅방 ID
     * @return 200 OK + 채팅방 상세 응답
     */
    @Operation(summary = "채팅방 상세 조회")
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoomDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(chatRoomService.getChatRoomDetail(userId, roomId)));
    }

    /**
     * 채팅방 이름 수정 — OWNER만 가능
     * @param userDetails 인증된 사용자 정보
     * @param roomId 수정할 채팅방 ID
     * @param request 변경할 이름 포함 요청
     * @return 200 OK + 수정된 채팅방 응답
     */
    @Operation(summary = "채팅방 이름 수정", description = "OWNER 권한 필요")
    @PatchMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> updateChatRoomName(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody UpdateChatRoomRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(chatRoomService.updateChatRoomName(userId, roomId, request)));
    }

    /**
     * 채팅방 멤버 초대 — 201 Created
     * @param userDetails 인증된 사용자 정보
     * @param roomId 초대할 채팅방 ID
     * @param request 초대할 회원 ID 포함 요청
     * @return 201 Created
     */
    @Operation(summary = "멤버 초대")
    @PostMapping("/{roomId}/members")
    public ResponseEntity<Void> inviteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody InviteMemberRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatRoomService.inviteMember(userId, roomId, request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 채팅방 자진 퇴장 — 204 No Content
     * @param userDetails 인증된 사용자 정보
     * @param roomId 퇴장할 채팅방 ID
     * @return 204 No Content
     */
    @Operation(summary = "채팅방 퇴장")
    @DeleteMapping("/{roomId}/members/me")
    public ResponseEntity<Void> leaveChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatRoomService.leaveChatRoom(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 멤버 강퇴 — OWNER만 가능, 204 No Content
     * @param userDetails 인증된 사용자 정보
     * @param roomId 채팅방 ID
     * @param targetUserId 강퇴할 회원 ID
     * @return 204 No Content
     */
    @Operation(summary = "멤버 강퇴", description = "OWNER 권한 필요")
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