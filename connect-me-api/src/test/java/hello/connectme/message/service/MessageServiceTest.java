package hello.connectme.message.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.chatroom.ChatRoomMember;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.chatroom.ChatRoomMemberRole;
import hello.connectme.domain.message.Message;
import hello.connectme.domain.message.MessageRepository;
import hello.connectme.domain.message.MessageType;
import hello.connectme.message.dto.MessageResponse;
import hello.connectme.message.dto.SendMessageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Test
    void sendMessage_textMessage_savesAndReturnsResponse() {
        Long senderId = 1L;
        Long chatRoomId = 10L;
        SendMessageRequest request = new SendMessageRequest(chatRoomId, "TEXT", "안녕하세요", null, null, null);
        ChatRoomMember member = ChatRoomMember.join(chatRoomId, senderId, ChatRoomMemberRole.MEMBER);
        Message saved = Message.create(chatRoomId, senderId, MessageType.TEXT, "안녕하세요", null, null, null);

        given(chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, senderId)).willReturn(Optional.of(member));
        given(messageRepository.save(any(Message.class))).willReturn(saved);

        MessageResponse response = messageService.sendMessage(senderId, request);

        assertThat(response.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.senderId()).isEqualTo(senderId);
        assertThat(response.type()).isEqualTo("TEXT");
        assertThat(response.content()).isEqualTo("안녕하세요");
    }

    @Test
    void sendMessage_notMember_throwsBusinessException() {
        Long senderId = 1L;
        Long chatRoomId = 10L;
        SendMessageRequest request = new SendMessageRequest(chatRoomId, "TEXT", "안녕하세요", null, null, null);

        given(chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, senderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(senderId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void deleteMessage_success_setsIsDeleted() {
        Long messageId = 100L;
        Long userId = 1L;
        Message message = Message.create(10L, userId, MessageType.TEXT, "내용", null, null, null);

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        MessageResponse response = messageService.deleteMessage(messageId, userId);

        assertThat(response.isDeleted()).isTrue();
    }

    @Test
    void deleteMessage_notSender_throwsBusinessException() {
        Long messageId = 100L;
        Long userId = 1L;
        Long anotherUserId = 2L;
        Message message = Message.create(10L, anotherUserId, MessageType.TEXT, "내용", null, null, null);

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.deleteMessage(messageId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MESSAGE_NOT_SENDER.getMessage());
    }

    @Test
    void deleteMessage_notFound_throwsBusinessException() {
        Long messageId = 999L;
        Long userId = 1L;

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.deleteMessage(messageId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MESSAGE_NOT_FOUND.getMessage());
    }
}