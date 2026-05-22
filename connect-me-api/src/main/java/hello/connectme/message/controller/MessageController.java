package hello.connectme.message.controller;

import hello.connectme.global.response.ApiResponse;
import hello.connectme.message.dto.MessagePageResponse;
import hello.connectme.message.dto.PinMessageRequest;
import hello.connectme.message.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메시지", description = "메시지 히스토리 조회, 삭제, 핀 고정 API")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/chat-rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<MessagePageResponse>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) Long cursorId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(messageService.getMessages(roomId, userId, cursorId, size)));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/chat-rooms/{roomId}/pin")
    public ResponseEntity<ApiResponse<Void>> pinMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestBody PinMessageRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        messageService.pinMessage(roomId, userId, request.messageId());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}