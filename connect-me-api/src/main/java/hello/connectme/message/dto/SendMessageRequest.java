package hello.connectme.message.dto;

/**
 * 메시지 전송 요청 DTO
 * WebSocket STOMP 메시지로 전달되며 type은 MessageType enum 값(TEXT/IMAGE/FILE)
 */
public record SendMessageRequest(
        Long roomId,
        String type,
        String content,
        String fileUrl,
        String fileName,
        Long fileSize
) {}