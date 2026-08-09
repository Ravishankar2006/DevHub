package com.devhub.tasks;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest extends BaseIntegrationTest {

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
    void createRequiresTitle() throws Exception {
        String token = registerAndLogin(uniqueEmail("task-validate"));

        mockMvc.perform(post("/tasks")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndGetTask() throws Exception {
        String token = registerAndLogin(uniqueEmail("task-crud"));
        String id = createTask(token, "Write tests");

        mockMvc.perform(get("/tasks/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void updateTaskStatus() throws Exception {
        String token = registerAndLogin(uniqueEmail("task-update"));
        String id = createTask(token, "Ship feature");

        String updateBody = objectMapper.writeValueAsString(Map.of("title", "Ship feature", "status", "DONE"));
        mockMvc.perform(put("/tasks/" + id)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void listFiltersByStatus() throws Exception {
        String token = registerAndLogin(uniqueEmail("task-filter"));
        String doneId = createTask(token, "Done task");
        createTask(token, "Pending task");

        mockMvc.perform(put("/tasks/" + doneId)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Done task", "status", "DONE"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tasks").header("Authorization", authHeader(token)).param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Done task"));
    }

    @Test
    void deleteTask() throws Exception {
        String token = registerAndLogin(uniqueEmail("task-delete"));
        String id = createTask(token, "Temp task");

        mockMvc.perform(delete("/tasks/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessTask() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("task-owner"));
        String otherToken = registerAndLogin(uniqueEmail("task-other"));
        String id = createTask(ownerToken, "Secret task");

        mockMvc.perform(get("/tasks/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/tasks/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
