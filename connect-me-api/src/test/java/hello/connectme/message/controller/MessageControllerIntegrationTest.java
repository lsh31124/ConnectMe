package hello.connectme.message.controller;

import hello.connectme.domain.chatroom.ChatRoom;
import hello.connectme.domain.chatroom.ChatRoomMember;
import hello.connectme.domain.chatroom.ChatRoomMemberRepository;
import hello.connectme.domain.chatroom.ChatRoomMemberRole;
import hello.connectme.domain.chatroom.ChatRoomRepository;
import hello.connectme.domain.message.Message;
import hello.connectme.domain.message.MessageRepository;
import hello.connectme.domain.message.MessageType;
import hello.connectme.domain.user.User;
import hello.connectme.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    UserRepository userRepository;

    private String accessToken;
    private Long userId;
    private Long chatRoomId;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, String> registerBody = Map.of(
                "email", "msgtest@email.com",
                "password", "password123",
                "name", "메시지테스터"
        );
        String registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andReturn().getResponse().getContentAsString();

        accessToken = objectMapper.readTree(registerResult)
                .path("data").path("accessToken").asText();

        User user = userRepository.findByEmail("msgtest@email.com").orElseThrow();
        userId = user.getId();

        ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup("테스트방", userId));
        chatRoomId = room.getId();
        chatRoomMemberRepository.save(ChatRoomMember.join(chatRoomId, userId, ChatRoomMemberRole.OWNER));
    }

    @Test
    void getMessages_success_returns200() throws Exception {
        mockMvc.perform(get("/chat-rooms/" + chatRoomId + "/messages")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.messages").isArray());
    }

    @Test
    void getMessages_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/chat-rooms/" + chatRoomId + "/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMessage_success_returns204() throws Exception {
        Message message = messageRepository.save(
                Message.create(chatRoomId, userId, MessageType.TEXT, "삭제할메시지", null, null, null)
        );

        mockMvc.perform(delete("/messages/" + message.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMessage_notSender_returns400() throws Exception {
        Map<String, String> otherRegisterBody = Map.of(
                "email", "other@email.com",
                "password", "password123",
                "name", "다른유저"
        );
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherRegisterBody)));

        User otherUser = userRepository.findByEmail("other@email.com").orElseThrow();

        Message message = messageRepository.save(
                Message.create(chatRoomId, otherUser.getId(), MessageType.TEXT, "다른사람메시지", null, null, null)
        );

        mockMvc.perform(delete("/messages/" + message.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MSG_002"));
    }

    @Test
    void deleteMessage_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/messages/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void pinMessage_success_returns200() throws Exception {
        Message message = messageRepository.save(
                Message.create(chatRoomId, userId, MessageType.TEXT, "핀고정메시지", null, null, null)
        );

        Map<String, Long> body = Map.of("messageId", message.getId());

        mockMvc.perform(patch("/chat-rooms/" + chatRoomId + "/pin")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void pinMessage_withoutToken_returnsForbidden() throws Exception {
        Map<String, Long> body = Map.of("messageId", 1L);

        mockMvc.perform(patch("/chat-rooms/" + chatRoomId + "/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}