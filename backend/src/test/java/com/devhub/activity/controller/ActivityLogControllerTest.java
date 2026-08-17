package com.devhub.activity.controller;

import com.devhub.ai.GeminiChatClient;
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

class ActivityLogControllerTest extends BaseIntegrationTest {

    private String createTask(String token, String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title));
        String response = mockMvc.perform(post("/tasks")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void userCreatedTaskWritesActivityLogWithSourceUser() throws Exception {
        String token = registerAndLogin(uniqueEmail("activity-user-create"));
        createTask(token, "Write the report");

        mockMvc.perform(get("/activity").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("USER"))
                .andExpect(jsonPath("$[0].actionType").value("CREATE_TASK"))
                .andExpect(jsonPath("$[0].summary").value("Created task \"Write the report\""));
    }

    @Test
    void updateAndDeleteEachAppendALogEntryNewestFirst() throws Exception {
        String token = registerAndLogin(uniqueEmail("activity-user-crud"));
        String id = createTask(token, "Ship feature");

        String updateBody = objectMapper.writeValueAsString(Map.of("title", "Ship feature", "status", "DONE"));
        mockMvc.perform(put("/tasks/" + id)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/tasks/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/activity").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].actionType").value("DELETE_TASK"))
                .andExpect(jsonPath("$[1].actionType").value("UPDATE_TASK"))
                .andExpect(jsonPath("$[2].actionType").value("CREATE_TASK"));
    }

    @Test
    void confirmedAiActionWritesActivityLogWithSourceAiAgent() throws Exception {
        when(geminiChatClient.sendAgentMessage(anyList(), any())).thenAnswer(invocation -> {
            GeminiChatClient.ToolInvoker toolInvoker = invocation.getArgument(1);
            toolInvoker.invoke("create_task", Map.of("title", "Draft the proposal"));
            return "Drafted that task for you to confirm.";
        });

        String token = registerAndLogin(uniqueEmail("activity-ai-confirm"));
        String conversationResponse = mockMvc.perform(post("/ai/conversations").header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(conversationResponse).get("id").asText();

        String sendResponse = mockMvc.perform(post("/ai/conversations/" + conversationId + "/messages")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Create a task called Draft the proposal"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String proposalId = objectMapper.readTree(sendResponse).get("pendingProposals").get(0).get("id").asText();

        // Proposing must not log anything yet - only a confirmed action counts as taken.
        mockMvc.perform(get("/activity").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/ai/proposals/" + proposalId + "/confirm").header("Authorization", authHeader(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/activity").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("AI_AGENT"))
                .andExpect(jsonPath("$[0].actionType").value("CREATE_TASK"));
    }

    @Test
    void activityLogIsScopedToOwner() throws Exception {
        String tokenA = registerAndLogin(uniqueEmail("activity-owner-a"));
        String tokenB = registerAndLogin(uniqueEmail("activity-owner-b"));
        createTask(tokenA, "Only mine");

        mockMvc.perform(get("/activity").header("Authorization", authHeader(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
