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
        boolean deleted,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getType().name(),
                message.isDeleted() ? null : message.getContent(),
                message.isDeleted() ? null : message.getFileUrl(),
                message.isDeleted() ? null : message.getFileName(),
                message.isDeleted() ? null : message.getFileSize(),
                message.isDeleted(),
                message.getCreatedAt()
        );
    }
}