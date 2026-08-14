package com.devhub.leetcode;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeetCodeControllerTest extends BaseIntegrationTest {

    @Test
    void accountReturnsDisconnectedByDefault() throws Exception {
        String token = registerAndLogin(uniqueEmail("lc-account"));

        mockMvc.perform(get("/leetcode/account").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.totalSolved").value(0));
    }

    @Test
    void syncFailsWhenNotConnected() throws Exception {
        String token = registerAndLogin(uniqueEmail("lc-sync"));

        mockMvc.perform(post("/leetcode/sync").header("Authorization", authHeader(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void connectWithBlankUsernameFails() throws Exception {
        String token = registerAndLogin(uniqueEmail("lc-blank"));

        mockMvc.perform(post("/leetcode/connect")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void connectWithUnknownUsernameFails() throws Exception {
        String token = registerAndLogin(uniqueEmail("lc-unknown"));
        when(leetCodeApiClient.fetchProfile(any())).thenReturn(null);

        mockMvc.perform(post("/leetcode/connect")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "does-not-exist"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void connectWithValidUsernamePopulatesStats() throws Exception {
        String token = registerAndLogin(uniqueEmail("lc-connect"));

        LeetCodeApiClient.MatchedUser profile = new LeetCodeApiClient.MatchedUser(
                "testcoder",
                new LeetCodeApiClient.Profile(12345L),
                new LeetCodeApiClient.SubmitStats(List.of(
                        new LeetCodeApiClient.AcSubmission("All", 50, 80),
                        new LeetCodeApiClient.AcSubmission("Easy", 30, 40),
                        new LeetCodeApiClient.AcSubmission("Medium", 18, 35),
                        new LeetCodeApiClient.AcSubmission("Hard", 2, 5))),
                new LeetCodeApiClient.UserCalendar("{}"));
        when(leetCodeApiClient.fetchProfile(any())).thenReturn(profile);

        mockMvc.perform(post("/leetcode/connect")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "testcoder"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.username").value("testcoder"))
                .andExpect(jsonPath("$.ranking").value(12345))
                .andExpect(jsonPath("$.totalSolved").value(50))
                .andExpect(jsonPath("$.easySolved").value(30))
                .andExpect(jsonPath("$.mediumSolved").value(18))
                .andExpect(jsonPath("$.hardSolved").value(2));

        mockMvc.perform(get("/leetcode/account").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.username").value("testcoder"));

        mockMvc.perform(delete("/leetcode/account").header("Authorization", authHeader(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/leetcode/account").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }
}
