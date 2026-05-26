package hello.connectme.friend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class FriendControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenA;
    private String tokenB;
    private Long userBId;

    @BeforeEach
    void setUp() throws Exception {
        tokenA = registerAndGetToken("a@email.com", "password123", "유저A");
        String resultB = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "b@email.com",
                                "password", "password123",
                                "name", "유저B"
                        ))))
                .andReturn().getResponse().getContentAsString();
        tokenB = objectMapper.readTree(resultB).path("data").path("accessToken").asText();

        String profileResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + tokenB))
                .andReturn().getResponse().getContentAsString();
        userBId = objectMapper.readTree(profileResult).path("data").path("id").asLong();
    }

    private String registerAndGetToken(String email, String password, String name) throws Exception {
        String result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "password", password, "name", name
                        ))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("accessToken").asText();
    }

    @Test
    void sendFriendRequest_success_returns201() throws Exception {
        mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void sendFriendRequest_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/friends/request/" + userBId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sendFriendRequest_duplicate_returns400() throws Exception {
        mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FRIEND_002"));
    }

    @Test
    void sendFriendRequest_selfRequest_returns400() throws Exception {
        String meResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        Long userAId = objectMapper.readTree(meResult).path("data").path("id").asLong();

        mockMvc.perform(post("/friends/request/" + userAId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FRIEND_004"));
    }

    @Test
    void acceptFriend_success_returns200() throws Exception {
        String reqResult = mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        Long friendId = objectMapper.readTree(reqResult).path("data").path("id").asLong();

        mockMvc.perform(patch("/friends/" + friendId + "/accept")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void rejectFriend_success_returns204() throws Exception {
        String reqResult = mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        Long friendId = objectMapper.readTree(reqResult).path("data").path("id").asLong();

        mockMvc.perform(patch("/friends/" + friendId + "/reject")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNoContent());
    }

    @Test
    void getFriends_success_returns200() throws Exception {
        mockMvc.perform(get("/friends")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getFriendRequests_success_returns200() throws Exception {
        mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andReturn();

        mockMvc.perform(get("/friends/requests")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("유저A"));
    }

    @Test
    void blockFriend_success_returns200() throws Exception {
        String reqResult = mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        Long friendId = objectMapper.readTree(reqResult).path("data").path("id").asLong();
        mockMvc.perform(patch("/friends/" + friendId + "/accept")
                        .header("Authorization", "Bearer " + tokenB))
                .andReturn();

        mockMvc.perform(patch("/friends/" + friendId + "/block")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BLOCKED"));
    }

    @Test
    void deleteFriend_success_returns204() throws Exception {
        String reqResult = mockMvc.perform(post("/friends/request/" + userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        Long friendId = objectMapper.readTree(reqResult).path("data").path("id").asLong();
        mockMvc.perform(patch("/friends/" + friendId + "/accept")
                        .header("Authorization", "Bearer " + tokenB))
                .andReturn();

        mockMvc.perform(delete("/friends/" + friendId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }
}