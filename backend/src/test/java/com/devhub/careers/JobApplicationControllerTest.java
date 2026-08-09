package com.devhub.careers;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobApplicationControllerTest extends BaseIntegrationTest {

    private String createApplication(String token, String company, String role) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("companyName", company, "roleTitle", role));
        String response = mockMvc.perform(post("/careers/applications")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createRequiresCompanyAndRole() throws Exception {
        String token = registerAndLogin(uniqueEmail("job-validate"));

        mockMvc.perform(post("/careers/applications")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPopulatesCompanyAndInitialHistory() throws Exception {
        String token = registerAndLogin(uniqueEmail("job-crud"));
        String id = createApplication(token, "Acme Corp", "Backend Engineer");

        mockMvc.perform(get("/careers/applications/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.name").value("Acme Corp"))
                .andExpect(jsonPath("$.roleTitle").value("Backend Engineer"))
                .andExpect(jsonPath("$.statusHistory.length()").value(1));
    }

    @Test
    void changeStatusAppendsHistory() throws Exception {
        String token = registerAndLogin(uniqueEmail("job-status"));
        String id = createApplication(token, "Globex", "Platform Engineer");

        mockMvc.perform(patch("/careers/applications/" + id + "/status")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INTERVIEWING", "note", "Phone screen scheduled"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEWING"))
                .andExpect(jsonPath("$.statusHistory.length()").value(2));

        // Confirm the batched list-fetch path also returns full history, not just the single-item path.
        mockMvc.perform(get("/careers/applications").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statusHistory.length()").value(2));
    }

    @Test
    void listFiltersByStatus() throws Exception {
        String token = registerAndLogin(uniqueEmail("job-filter"));
        createApplication(token, "CompanyA", "Role A");
        String id2 = createApplication(token, "CompanyB", "Role B");

        mockMvc.perform(patch("/careers/applications/" + id2 + "/status")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "REJECTED"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/careers/applications").header("Authorization", authHeader(token)).param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company.name").value("CompanyB"));
    }

    @Test
    void deleteApplication() throws Exception {
        String token = registerAndLogin(uniqueEmail("job-delete"));
        String id = createApplication(token, "TempCo", "Temp Role");

        mockMvc.perform(delete("/careers/applications/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/careers/applications/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessApplication() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("job-owner"));
        String otherToken = registerAndLogin(uniqueEmail("job-other"));
        String id = createApplication(ownerToken, "Private Co", "Secret Role");

        mockMvc.perform(get("/careers/applications/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/careers/applications/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
