package hello.connectme.message.dto;

public record SendMessageRequest(
        Long roomId,
        String type,
        String content,
        String fileUrl,
        String fileName,
        Long fileSize
) {}