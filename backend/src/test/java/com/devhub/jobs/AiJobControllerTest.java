package com.devhub.jobs;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiJobControllerTest extends BaseIntegrationTest {

    private String triggerResumeReviewJob(String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1.4".getBytes());
        String uploadResponse = mockMvc.perform(multipart("/resumes")
                        .file(file)
                        .param("name", "Job Test Resume")
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String resumeId = objectMapper.readTree(uploadResponse).get("id").asText();

        String jobResponse = mockMvc.perform(post("/resumes/" + resumeId + "/review").header("Authorization", authHeader(token)))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(jobResponse).get("id").asText();
    }

    @Test
    void getOwnJobReturnsStatus() throws Exception {
        String token = registerAndLogin(uniqueEmail("job-status-owner"));
        String jobId = triggerResumeReviewJob(token);

        mockMvc.perform(get("/jobs/" + jobId).header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobType").value("RESUME_REVIEW"));
    }

    @Test
    void otherUserCannotAccessJob() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail("job-owner2"));
        String otherToken = registerAndLogin(uniqueEmail("job-other2"));
        String jobId = triggerResumeReviewJob(ownerToken);

        mockMvc.perform(get("/jobs/" + jobId).header("Authorization", authHeader(otherToken)))
                .andExpect(status().isNotFound());
    }
}
