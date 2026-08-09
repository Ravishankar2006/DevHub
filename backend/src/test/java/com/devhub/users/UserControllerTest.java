package com.devhub.users;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseIntegrationTest {

    @Test
    void getMeReturnsCurrentUser() throws Exception {
        String email = uniqueEmail("me");
        String token = registerAndLogin(email);

        mockMvc.perform(get("/users/me").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.theme").value("light"));
    }

    @Test
    void updateThemePersists() throws Exception {
        String token = registerAndLogin(uniqueEmail("theme"));

        mockMvc.perform(put("/users/me/theme")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("theme", "dark"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"));

        mockMvc.perform(get("/users/me").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"));
    }

    @Test
    void getMeRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }
}
