package hello.connectme.message.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.chatroom.ChatRoom;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.chatroom.ChatRoomRepository;
import hello.connectme.domain.message.Message;
import hello.connectme.domain.message.MessageRepository;
import hello.connectme.domain.message.MessageType;
import hello.connectme.message.dto.MessagePageResponse;
import hello.connectme.message.dto.MessageResponse;
import hello.connectme.message.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(request.roomId(), senderId)
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

    public MessagePageResponse getMessages(Long roomId, Long userId, Long cursorId, int size) {
        chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)

                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(0, size);
        List<Message> messages;
        if (cursorId == null) {
            messages = messageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageRequest);
        } else {
            messages = messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(roomId, cursorId, pageRequest);
        }

        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::from)
                .toList();

        Long nextCursor = messages.size() == size ? messages.getLast().getId() : null;
        return new MessagePageResponse(responses, nextCursor);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_SENDER);
        }

        message.softDelete();
    }

    @Transactional
    public void pinMessage(Long roomId, Long userId, Long messageId) {
        chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)

                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getChatRoomId().equals(roomId)) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoom.pinMessage(message.getId());
    }
}