package com.devhub.resumes;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResumeControllerTest extends BaseIntegrationTest {

    private MockMultipartFile pdfFile(String filename) {
        return new MockMultipartFile("file", filename, "application/pdf", "%PDF-1.4 fake content".getBytes());
    }

    private String uploadResume(String token, String name) throws Exception {
        String response = mockMvc.perform(multipart("/resumes")
                        .file(pdfFile("resume.pdf"))
                        .param("name", name)
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void uploadRejectsNonPdfFile() throws Exception {
        String token = registerAndLogin(uniqueEmail("resume-validate"));
        MockMultipartFile txtFile = new MockMultipartFile("file", "resume.txt", "text/plain", "not a pdf".getBytes());

        mockMvc.perform(multipart("/resumes")
                        .file(txtFile)
                        .param("name", "Bad Resume")
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAndGetResume() throws Exception {
        String token = registerAndLogin(uniqueEmail("resume-crud"));
        String id = uploadResume(token, "Backend Resume v1");

        mockMvc.perform(get("/resumes/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Backend Resume v1"))
                .andExpect(jsonPath("$.fileName").value("resume.pdf"));
    }

    @Test
    void updateResumeMetadata() throws Exception {
        String token = registerAndLogin(uniqueEmail("resume-update"));
        String id = uploadResume(token, "Original Name");

        String updateBody = objectMapper.writeValueAsString(java.util.Map.of("name", "Updated Name", "label", "backend"));
        mockMvc.perform(put("/resumes/" + id)
                        .header("Authorization", authHeader(token))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.label").value("backend"));
    }

    @Test
    void triggerReviewReturnsAcceptedJob() throws Exception {
        String token = registerAndLogin(uniqueEmail("resume-review"));
        String id = uploadResume(token, "Review Me");

        mockMvc.perform(post("/resumes/" + id + "/review").header("Authorization", authHeader(token)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobType").value("RESUME_REVIEW"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void downloadReturnsFileBytes() throws Exception {
        String token = registerAndLogin(uniqueEmail("resume-download"));
        String id = uploadResume(token, "Downloadable");

        mockMvc.perform(get("/resumes/" + id + "/download").header("Authorization", authHeader(token)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteResume() throws Exception {
        String token = registerAndLogin(uniqueEmail("resume-delete"));
        String id = uploadResume(token, "Temp Resume");

        mockMvc.perform(delete("/resumes/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/resumes/" + id).header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessResume() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("resume-owner"));
        String otherToken = registerAndLogin(uniqueEmail("resume-other"));
        String id = uploadResume(ownerToken, "Private Resume");

        mockMvc.perform(get("/resumes/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/resumes/" + id + "/download").header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/resumes/" + id).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
