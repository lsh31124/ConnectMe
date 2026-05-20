package hello.connectme.chatroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatRoomControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String accessToken;
    private String targetToken;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = registerAndLogin("chattest@email.com", "password123", "채팅유저");
        targetToken = registerAndLogin("target@email.com", "password123", "대상유저");
    }

    private String registerAndLogin(String email, String password, String name) throws Exception {
        Map<String, String> registerBody = Map.of("email", email, "password", password, "name", name);
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody)));

        Map<String, String> loginBody = Map.of("email", email, "password", password);
        String result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("accessToken").asText();
    }

    private Long getMyUserId(String token) throws Exception {
        String result = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("id").asLong();
    }

    @Test
    void createDirectRoom_success_returns201() throws Exception {
        Long targetUserId = getMyUserId(targetToken);
        Map<String, Long> body = Map.of("targetUserId", targetUserId);

        mockMvc.perform(post("/chat-rooms/direct")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("DIRECT"))
                .andExpect(jsonPath("$.data.memberCount").value(2));
    }

    @Test
    void createDirectRoom_withoutToken_returnsForbidden() throws Exception {
        Map<String, Long> body = Map.of("targetUserId", 2L);

        mockMvc.perform(post("/chat-rooms/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createGroupRoom_success_returns201() throws Exception {
        Long targetUserId = getMyUserId(targetToken);
        Map<String, Object> body = Map.of("name", "테스트 그룹방", "memberIds", List.of(targetUserId));

        mockMvc.perform(post("/chat-rooms/group")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("GROUP"))
                .andExpect(jsonPath("$.data.name").value("테스트 그룹방"));
    }

    @Test
    void getMyChatRooms_success_returns200() throws Exception {
        Long targetUserId = getMyUserId(targetToken);
        Map<String, Long> body = Map.of("targetUserId", targetUserId);
        mockMvc.perform(post("/chat-rooms/direct")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

        mockMvc.perform(get("/chat-rooms")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getMyChatRooms_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/chat-rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getChatRoomDetail_success_returns200() throws Exception {
        Long targetUserId = getMyUserId(targetToken);
        Map<String, Long> createBody = Map.of("targetUserId", targetUserId);
        String createResult = mockMvc.perform(post("/chat-rooms/direct")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andReturn().getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        mockMvc.perform(get("/chat-rooms/" + roomId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(roomId))
                .andExpect(jsonPath("$.data.members").isArray());
    }

    @Test
    @Rollback
    void leaveChatRoom_success_returns204() throws Exception {
        Long targetUserId = getMyUserId(targetToken);
        Map<String, Long> createBody = Map.of("targetUserId", targetUserId);
        String createResult = mockMvc.perform(post("/chat-rooms/direct")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andReturn().getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        mockMvc.perform(delete("/chat-rooms/" + roomId + "/members/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
}