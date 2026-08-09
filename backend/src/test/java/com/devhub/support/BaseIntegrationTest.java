package com.devhub.support;

import com.devhub.ai.GeminiChatClient;
import com.devhub.ai.GeminiEmbeddingClient;
import com.devhub.github.GitHubApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Every controller test extends this shared base so the Spring context (and its mocked
// external clients) is reused across test classes instead of rebuilt per class.
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected GeminiChatClient geminiChatClient;

    @MockBean
    protected GeminiEmbeddingClient geminiEmbeddingClient;

    @MockBean
    protected GitHubApiClient gitHubApiClient;

    // Every test that registers a user gets its own X-Forwarded-For so the shared
    // RateLimitFilter state (one bean instance per cached Spring context) never causes
    // unrelated tests to start seeing 429s once enough tests have run.
    protected String registerAndLogin(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Test User",
                "email", email,
                "password", "TestPass123!"));

        String response = mockMvc.perform(post("/auth/register")
                        .header("X-Forwarded-For", uniqueTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    protected String uniqueTestIp() {
        return "10.0." + (int) (Math.random() * 255) + "." + (int) (Math.random() * 255);
    }

    protected String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.devhub";
    }

    protected String authHeader(String token) {
        return "Bearer " + token;
    }
}
