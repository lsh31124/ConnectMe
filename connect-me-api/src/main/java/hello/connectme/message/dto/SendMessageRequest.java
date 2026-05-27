package hello.connectme.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 메시지 전송 요청 DTO
 * WebSocket STOMP 메시지로 전달되며 type은 MessageType enum 값(TEXT/IMAGE/FILE)
 */
public record SendMessageRequest(
        @NotNull Long roomId,
        @NotBlank @Pattern(regexp = "TEXT|IMAGE|FILE", message = "메시지 타입은 TEXT, IMAGE, FILE 중 하나여야 합니다") String type,
        @Size(max = 4000, message = "메시지 내용은 4000자 이하여야 합니다") String content,
        @Size(max = 2048) String fileUrl,
        @Size(max = 255) String fileName,
        Long fileSize
) {}