package com.devhub.learning;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LearningResourceControllerTest extends BaseIntegrationTest {

    private String createResource(String token, String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", title));
        String response = mockMvc.perform(post("/learning")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresTitle() throws Exception {
        String token = registerAndLogin(uniqueEmail("learn-validate"));

        mockMvc.perform(post("/learning")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndGetResource() throws Exception {
        String token = registerAndLogin(uniqueEmail("learn-crud"));
        String id = createResource(token, "Spring Boot course");

        mockMvc.perform(get("/learning/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot course"));
    }

    @Test
    void updateResourceStatus() throws Exception {
        String token = registerAndLogin(uniqueEmail("learn-update"));
        String id = createResource(token, "React course");

        String updateBody = objectMapper.writeValueAsString(Map.of("title", "React course", "status", "COMPLETED"));
        mockMvc.perform(put("/learning/" + id)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteResource() throws Exception {
        String token = registerAndLogin(uniqueEmail("learn-delete"));
        String id = createResource(token, "Temp course");

        mockMvc.perform(delete("/learning/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/learning/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessResource() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("learn-owner"));
        String otherToken = registerAndLogin(uniqueEmail("learn-other"));
        String id = createResource(ownerToken, "Private course");

        mockMvc.perform(get("/learning/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/learning/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
