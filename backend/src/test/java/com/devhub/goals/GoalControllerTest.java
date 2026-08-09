package com.devhub.goals;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GoalControllerTest extends BaseIntegrationTest {

    private String createGoal(String token, String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title));
        String response = mockMvc.perform(post("/goals")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresTitle() throws Exception {
        String token = registerAndLogin(uniqueEmail("goal-validate"));

        mockMvc.perform(post("/goals")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndGetGoal() throws Exception {
        String token = registerAndLogin(uniqueEmail("goal-crud"));
        String id = createGoal(token, "Learn Rust");

        mockMvc.perform(get("/goals/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Learn Rust"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateGoal() throws Exception {
        String token = registerAndLogin(uniqueEmail("goal-update"));
        String id = createGoal(token, "Original goal");

        String updateBody = objectMapper.writeValueAsString(Map.of("title", "Original goal", "status", "COMPLETED"));
        mockMvc.perform(put("/goals/" + id)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteGoal() throws Exception {
        String token = registerAndLogin(uniqueEmail("goal-delete"));
        String id = createGoal(token, "Temp goal");

        mockMvc.perform(delete("/goals/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/goals/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessGoal() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("goal-owner"));
        String otherToken = registerAndLogin(uniqueEmail("goal-other"));
        String id = createGoal(ownerToken, "Private goal");

        mockMvc.perform(get("/goals/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/goals/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
