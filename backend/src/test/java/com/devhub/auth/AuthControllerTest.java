package com.devhub.auth;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseIntegrationTest {

    @Test
    void registerReturnsAccessTokenAndUser() throws Exception {
        String email = uniqueEmail("register");
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "New User", "email", email, "password", "TestPass123!"));

        mockMvc.perform(post("/auth/register")
                        .header("X-Forwarded-For", uniqueTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail("dupe");
        registerAndLogin(email);

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Another User", "email", email, "password", "TestPass123!"));

        mockMvc.perform(post("/auth/register")
                        .header("X-Forwarded-For", uniqueTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginSucceedsWithCorrectCredentials() throws Exception {
        String email = uniqueEmail("login");
        registerAndLogin(email);

        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", "TestPass123!"));

        mockMvc.perform(post("/auth/login")
                        .header("X-Forwarded-For", uniqueTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        String email = uniqueEmail("wrongpass");
        registerAndLogin(email);

        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", "WrongPass123!"));

        mockMvc.perform(post("/auth/login")
                        .header("X-Forwarded-For", uniqueTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshIssuesNewAccessToken() throws Exception {
        String email = uniqueEmail("refresh");
        String ip = uniqueTestIp();
        String registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Refresh User", "email", email, "password", "TestPass123!"));

        String registerResponse = mockMvc.perform(post("/auth/register")
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(registerResponse).get("refreshToken").asText();

        mockMvc.perform(post("/auth/refresh")
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void loginRateLimitTripsAfterFiveAttempts() throws Exception {
        String ip = uniqueTestIp();
        String body = objectMapper.writeValueAsString(Map.of("email", "nobody@nowhere.test", "password", "WrongPass123!"));

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                            .header("X-Forwarded-For", ip)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().is4xxClientError())
                    .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                            429, result.getResponse().getStatus()));
        }

        mockMvc.perform(post("/auth/login")
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }
}
