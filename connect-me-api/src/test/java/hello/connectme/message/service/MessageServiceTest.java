package hello.connectme.message.service;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.common.exception.ErrorCode;
import hello.connectme.domain.chatroom.ChatRoom;
import hello.connectme.domain.chatroom.ChatRoomMember;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.chatroom.ChatRoomMemberRole;
import hello.connectme.domain.chatroom.ChatRoomRepository;
import hello.connectme.domain.message.Message;
import hello.connectme.domain.message.MessageRepository;
import hello.connectme.domain.message.MessageType;
import hello.connectme.message.dto.MessagePageResponse;
import hello.connectme.message.dto.SendMessageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    private Message createTestMessage(Long id, Long chatRoomId, Long senderId) {
        Message message = Message.create(chatRoomId, senderId, MessageType.TEXT, "내용", null, null, null);
        ReflectionTestUtils.setField(message, "id", id);
        return message;
    }

    private ChatRoomMember createTestMember(Long id, Long chatRoomId, Long userId) {
        ChatRoomMember member = ChatRoomMember.join(chatRoomId, userId, ChatRoomMemberRole.MEMBER);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private ChatRoom createTestChatRoom(Long id) {
        ChatRoom chatRoom = ChatRoom.createDirect(1L);
        ReflectionTestUtils.setField(chatRoom, "id", id);
        return chatRoom;
    }

    @Test
    void sendMessage_textMessage_savesAndReturnsResponse() {
        Long senderId = 1L;
        Long chatRoomId = 10L;
        SendMessageRequest request = new SendMessageRequest(chatRoomId, "TEXT", "안녕하세요", null, null, null);
        ChatRoomMember member = createTestMember(1L, chatRoomId, senderId);
        Message saved = createTestMessage(100L, chatRoomId, senderId);

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(chatRoomId, senderId)).willReturn(Optional.of(member));
        given(messageRepository.save(any(Message.class))).willReturn(saved);

        var response = messageService.sendMessage(senderId, request);

        assertThat(response.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.senderId()).isEqualTo(senderId);
        assertThat(response.type()).isEqualTo("TEXT");
    }

    @Test
    void sendMessage_notMember_throwsBusinessException() {
        Long senderId = 1L;
        Long chatRoomId = 10L;
        SendMessageRequest request = new SendMessageRequest(chatRoomId, "TEXT", "안녕하세요", null, null, null);

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(chatRoomId, senderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(senderId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void getMessages_firstPage_returnsMessages() {
        Long roomId = 10L;
        Long userId = 1L;
        int size = 2;
        ChatRoomMember member = createTestMember(1L, roomId, userId);
        ReflectionTestUtils.setField(member, "leftAt", null);
        Message msg1 = createTestMessage(5L, roomId, userId);
        Message msg2 = createTestMessage(4L, roomId, userId);

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)).willReturn(Optional.of(member));
        given(messageRepository.findByChatRoomIdOrderByIdDesc(eq(roomId), any(PageRequest.class)))
                .willReturn(List.of(msg1, msg2));

        MessagePageResponse response = messageService.getMessages(roomId, userId, null, size);

        assertThat(response.messages()).hasSize(2);
        assertThat(response.nextCursor()).isEqualTo(4L);
    }

    @Test
    void getMessages_withCursor_returnsOlderMessages() {
        Long roomId = 10L;
        Long userId = 1L;
        Long cursorId = 5L;
        int size = 30;
        ChatRoomMember member = createTestMember(1L, roomId, userId);
        ReflectionTestUtils.setField(member, "leftAt", null);
        Message msg = createTestMessage(3L, roomId, userId);

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)).willReturn(Optional.of(member));
        given(messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(eq(roomId), eq(cursorId), any(PageRequest.class)))
                .willReturn(List.of(msg));

        MessagePageResponse response = messageService.getMessages(roomId, userId, cursorId, size);

        assertThat(response.messages()).hasSize(1);
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void getMessages_leftMember_throwsBusinessException() {
        Long roomId = 10L;
        Long userId = 1L;

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessages(roomId, userId, null, 30))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void deleteMessage_success_softDeletesMessage() {
        Long messageId = 100L;
        Long userId = 1L;
        Message message = createTestMessage(messageId, 10L, userId);

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        messageService.deleteMessage(messageId, userId);

        assertThat(message.isDeleted()).isTrue();
    }

    @Test
    void deleteMessage_notSender_throwsBusinessException() {
        Long messageId = 100L;
        Long userId = 1L;
        Long anotherUserId = 2L;
        Message message = createTestMessage(messageId, 10L, anotherUserId);

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

    @Test
    void pinMessage_success_callsChatRoomPinMessage() {
        Long roomId = 10L;
        Long userId = 1L;
        Long messageId = 100L;
        ChatRoomMember member = createTestMember(1L, roomId, userId);
        ReflectionTestUtils.setField(member, "leftAt", null);
        Message message = createTestMessage(messageId, roomId, userId);
        ChatRoom chatRoom = createTestChatRoom(roomId);

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)).willReturn(Optional.of(member));
        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        messageService.pinMessage(roomId, userId, messageId);

        assertThat(chatRoom.getPinnedMessageId()).isEqualTo(messageId);
    }

    @Test
    void pinMessage_messageNotInRoom_throwsBusinessException() {
        Long roomId = 10L;
        Long userId = 1L;
        Long messageId = 100L;
        Long otherRoomId = 99L;
        ChatRoomMember member = createTestMember(1L, roomId, userId);
        ReflectionTestUtils.setField(member, "leftAt", null);
        Message message = createTestMessage(messageId, otherRoomId, userId);

        given(chatRoomMemberRepository.findFirstByChatRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)).willReturn(Optional.of(member));
        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.pinMessage(roomId, userId, messageId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MESSAGE_NOT_FOUND.getMessage());
    }
}