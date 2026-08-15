package com.devhub.ai;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AIConversationControllerTest extends BaseIntegrationTest {

    private String createConversation(String token) throws Exception {
        String response = mockMvc.perform(post("/ai/conversations").header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createConversationStartsEmpty() throws Exception {
        String token = registerAndLogin(uniqueEmail("ai-create"));

        mockMvc.perform(post("/ai/conversations").header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messages.length()").value(0));
    }

    @Test
    void sendMessageAppendsUserAndAssistantMessages() throws Exception {
        when(geminiChatClient.sendAgentMessage(anyList(), any())).thenReturn("Mocked assistant reply");

        String token = registerAndLogin(uniqueEmail("ai-message"));
        String id = createConversation(token);

        mockMvc.perform(post("/ai/conversations/" + id + "/messages")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Hello there"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].content").value("Hello there"))
                .andExpect(jsonPath("$.messages[1].content").value("Mocked assistant reply"));
    }

    @Test
    void deleteConversation() throws Exception {
        String token = registerAndLogin(uniqueEmail("ai-delete"));
        String id = createConversation(token);

        mockMvc.perform(delete("/ai/conversations/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/ai/conversations/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessConversation() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("ai-owner"));
        String otherToken = registerAndLogin(uniqueEmail("ai-other"));
        String id = createConversation(ownerToken);

        mockMvc.perform(get("/ai/conversations/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/ai/conversations/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
