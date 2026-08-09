package com.devhub.brief;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyBriefControllerTest extends BaseIntegrationTest {

    @Test
    void generateReturnsAcceptedJob() throws Exception {
        String token = registerAndLogin(uniqueEmail("brief-generate"));

        mockMvc.perform(post("/brief/generate").header("Authorization", authHeader(token)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobType").value("DAILY_BRIEF"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void todayReturnsNotFoundBeforeGeneration() throws Exception {
        String token = registerAndLogin(uniqueEmail("brief-today"));

        mockMvc.perform(get("/brief/today").header("Authorization", authHeader(token)))
                .andExpect(status().isNotFound());
    }
}
