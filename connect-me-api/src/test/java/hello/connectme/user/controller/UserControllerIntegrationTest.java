package hello.connectme.user.controller;

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
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, String> registerBody = Map.of(
                "email", "user@email.com",
                "password", "password123",
                "name", "홍길동"
        );
        String result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andReturn().getResponse().getContentAsString();

        accessToken = objectMapper.readTree(result)
                .path("data").path("accessToken").asText();
    }

    @Test
    void getMyProfile_success_returns200WithUserInfo() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("user@email.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"));
    }

    @Test
    void getMyProfile_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_invalidToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateMyProfile_success_returnsUpdatedProfile() throws Exception {
        Map<String, String> updateBody = Map.of(
                "name", "새이름",
                "statusMessage", "안녕하세요"
        );

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("새이름"))
                .andExpect(jsonPath("$.data.statusMessage").value("안녕하세요"));
    }

    @Test
    void updateMyProfile_withoutToken_returnsForbidden() throws Exception {
        Map<String, String> updateBody = Map.of("name", "새이름");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_byEmail_returnsMatchingUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .param("query", "user@email.com")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].email").value("user@email.com"));
    }

    @Test
    void searchUsers_noMatch_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/users")
                        .param("query", "nomatch@email.com")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deleteMyAccount_success_returns204() throws Exception {
        mockMvc.perform(delete("/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMyAccount_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/users/me"))
                .andExpect(status().isForbidden());
    }

}