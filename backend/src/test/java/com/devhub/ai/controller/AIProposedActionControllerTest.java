package com.devhub.ai.controller;

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

class AIProposedActionControllerTest extends BaseIntegrationTest {

    private String createConversation(String token) throws Exception {
        String response = mockMvc.perform(post("/ai/conversations").header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void confirmUnknownProposalReturns404() throws Exception {
        String token = registerAndLogin(uniqueEmail("proposal-404"));

        mockMvc.perform(post("/ai/proposals/00000000-0000-0000-0000-000000000000/confirm")
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectUnknownProposalReturns404() throws Exception {
        String token = registerAndLogin(uniqueEmail("proposal-reject-404"));

        mockMvc.perform(post("/ai/proposals/00000000-0000-0000-0000-000000000000/reject")
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullProposeConfirmLoopCreatesTaskOnlyAfterConfirm() throws Exception {
        when(geminiChatClient.sendAgentMessage(anyList(), any())).thenAnswer(invocation -> {
            GeminiChatClient.ToolInvoker toolInvoker = invocation.getArgument(1);
            toolInvoker.invoke("create_task", Map.of("title", "Ship the release"));
            return "I've drafted that task for you to confirm.";
        });

        String token = registerAndLogin(uniqueEmail("proposal-loop"));
        String conversationId = createConversation(token);

        String sendResponse = mockMvc.perform(post("/ai/conversations/" + conversationId + "/messages")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Create a task called Ship the release"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingProposals.length()").value(1))
                .andReturn().getResponse().getContentAsString();

        String proposalId = objectMapper.readTree(sendResponse).get("pendingProposals").get(0).get("id").asText();

        mockMvc.perform(post("/ai/proposals/" + proposalId + "/confirm")
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/ai/conversations/" + conversationId)
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingProposals.length()").value(0));

        mockMvc.perform(get("/tasks").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Ship the release"));
    }

    @Test
    void rejectingDestructiveProposalLeavesTargetUntouched() throws Exception {
        when(geminiChatClient.sendAgentMessage(anyList(), any())).thenAnswer(invocation -> {
            GeminiChatClient.ToolInvoker toolInvoker = invocation.getArgument(1);
            toolInvoker.invoke("create_task", Map.of("title", "Keep me"));
            return "Created a proposal.";
        }).thenAnswer(invocation -> {
            GeminiChatClient.ToolInvoker toolInvoker = invocation.getArgument(1);
            toolInvoker.invoke("delete_task", Map.of("title", "Keep me"));
            return "Drafted a delete for you to confirm.";
        });

        String token = registerAndLogin(uniqueEmail("proposal-reject-loop"));
        String conversationId = createConversation(token);

        String createResponse = mockMvc.perform(post("/ai/conversations/" + conversationId + "/messages")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Create a task called Keep me"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String createProposalId = objectMapper.readTree(createResponse).get("pendingProposals").get(0).get("id").asText();
        mockMvc.perform(post("/ai/proposals/" + createProposalId + "/confirm").header("Authorization", authHeader(token)))
                .andExpect(status().isOk());

        String deleteResponse = mockMvc.perform(post("/ai/conversations/" + conversationId + "/messages")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Delete the task called Keep me"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingProposals[0].destructive").value(true))
                .andReturn().getResponse().getContentAsString();
        String deleteProposalId = objectMapper.readTree(deleteResponse).get("pendingProposals").get(0).get("id").asText();

        mockMvc.perform(post("/ai/proposals/" + deleteProposalId + "/reject").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(get("/tasks").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Keep me"));
    }
}
