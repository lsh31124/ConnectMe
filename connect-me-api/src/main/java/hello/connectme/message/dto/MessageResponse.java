package hello.connectme.message.dto;

import hello.connectme.domain.message.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String type,
        String content,
        String fileUrl,
        String fileName,
        Long fileSize,
        boolean isDeleted,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getType().name(),
                message.getContent(),
                message.getFileUrl(),
                message.getFileName(),
                message.getFileSize(),
                message.isDeleted(),
                message.getCreatedAt()
        );
    }
}