package hello.connectme.message.integration;

import hello.connectme.common.exception.BusinessException;
import hello.connectme.domain.chatroom.ChatRoomMember;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.chatroom.ChatRoomMemberRole;
import hello.connectme.domain.message.Message;
import hello.connectme.domain.message.MessageRepository;
import hello.connectme.message.dto.MessageResponse;
import hello.connectme.message.dto.SendMessageRequest;
import hello.connectme.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MessageServiceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Test
    void sendMessage_savesMessageToDb() {
        Long chatRoomId = 1L;
        Long senderId = 1L;
        chatRoomMemberRepository.save(ChatRoomMember.join(chatRoomId, senderId, ChatRoomMemberRole.MEMBER));

        SendMessageRequest request = new SendMessageRequest(chatRoomId, "TEXT", "통합 테스트 메시지", null, null, null);

        MessageResponse response = messageService.sendMessage(senderId, request);

        assertThat(response.id()).isNotNull();
        assertThat(response.content()).isEqualTo("통합 테스트 메시지");
        assertThat(response.type()).isEqualTo("TEXT");

        List<Message> messages = messageRepository.findAll();
        assertThat(messages).anyMatch(m -> m.getContent().equals("통합 테스트 메시지"));
    }

    @Test
    void deleteMessage_softDeletesMessage() {
        Long chatRoomId = 1L;
        Long senderId = 1L;
        chatRoomMemberRepository.save(ChatRoomMember.join(chatRoomId, senderId, ChatRoomMemberRole.MEMBER));

        SendMessageRequest request = new SendMessageRequest(chatRoomId, "TEXT", "삭제할 메시지", null, null, null);
        MessageResponse created = messageService.sendMessage(senderId, request);

        MessageResponse deleted = messageService.deleteMessage(created.id(), senderId);

        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    void sendMessage_leftMember_throwsBusinessException() {
        Long chatRoomId = 2L;
        Long senderId = 2L;

        assertThatThrownBy(() -> messageService.sendMessage(senderId,
                new SendMessageRequest(chatRoomId, "TEXT", "권한 없음", null, null, null)))
                .isInstanceOf(BusinessException.class);
    }
}