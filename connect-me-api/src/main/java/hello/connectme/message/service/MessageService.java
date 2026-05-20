package hello.connectme.message.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.message.Message;
import hello.connectme.domain.message.MessageRepository;
import hello.connectme.domain.message.MessageType;
import hello.connectme.message.dto.MessageResponse;
import hello.connectme.message.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        chatRoomMemberRepository.findByChatRoomIdAndUserId(request.roomId(), senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        Message message = Message.create(
                request.roomId(),
                senderId,
                MessageType.valueOf(request.type()),
                request.content(),
                request.fileUrl(),
                request.fileName(),
                request.fileSize()
        );

        return MessageResponse.from(messageRepository.save(message));
    }

    @Transactional
    public MessageResponse deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_SENDER);
        }

        message.softDelete();
        return MessageResponse.from(message);
    }
}