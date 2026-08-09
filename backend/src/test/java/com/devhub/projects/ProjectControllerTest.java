package com.devhub.projects;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectControllerTest extends BaseIntegrationTest {

    private String createProject(String token, String name) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", name));
        String response = mockMvc.perform(post("/projects")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresName() throws Exception {
        String token = registerAndLogin(uniqueEmail("proj-validate"));

        mockMvc.perform(post("/projects")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndGetProject() throws Exception {
        String token = registerAndLogin(uniqueEmail("proj-crud"));
        String id = createProject(token, "DevHub");

        mockMvc.perform(get("/projects/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("DevHub"))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void listReturnsCreatedProjects() throws Exception {
        String token = registerAndLogin(uniqueEmail("proj-list"));
        createProject(token, "Alpha");
        createProject(token, "Beta");

        mockMvc.perform(get("/projects").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateProject() throws Exception {
        String token = registerAndLogin(uniqueEmail("proj-update"));
        String id = createProject(token, "Original");

        String updateBody = objectMapper.writeValueAsString(Map.of("name", "Renamed", "archived", true));
        mockMvc.perform(put("/projects/" + id)
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    void deleteProject() throws Exception {
        String token = registerAndLogin(uniqueEmail("proj-delete"));
        String id = createProject(token, "ToDelete");

        mockMvc.perform(delete("/projects/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/projects/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessProject() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("proj-owner"));
        String otherToken = registerAndLogin(uniqueEmail("proj-other"));
        String id = createProject(ownerToken, "Private Project");

        mockMvc.perform(get("/projects/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/projects/" + id)
                        .header("Authorization", authHeader(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Hijacked"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/projects/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void milestoneCrudAndOwnershipIsolation() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("milestone-owner"));
        String otherToken = registerAndLogin(uniqueEmail("milestone-other"));
        String projectId = createProject(ownerToken, "Milestone Project");

        String createBody = objectMapper.writeValueAsString(Map.of("title", "Launch v1"));
        String createResponse = mockMvc.perform(post("/projects/" + projectId + "/milestones")
                        .header("Authorization", authHeader(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Launch v1"))
                .andReturn().getResponse().getContentAsString();
        String milestoneId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/projects/" + projectId + "/milestones").header("Authorization", authHeader(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Other user can't even list milestones under a project they don't own.
        mockMvc.perform(get("/projects/" + projectId + "/milestones").header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/projects/" + projectId + "/milestones/" + milestoneId)
                        .header("Authorization", authHeader(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Hijacked"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/projects/" + projectId + "/milestones/" + milestoneId)
                        .header("Authorization", authHeader(ownerToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void milestoneOverviewListsAcrossAllProjects() throws Exception {
        String token = registerAndLogin(uniqueEmail("milestone-overview"));
        String projectAId = createProject(token, "Project A");
        String projectBId = createProject(token, "Project B");

        mockMvc.perform(post("/projects/" + projectAId + "/milestones")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "A milestone"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/projects/" + projectBId + "/milestones")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "B milestone"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/milestones").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
