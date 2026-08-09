package com.devhub.github;

import com.devhub.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GitHubControllerTest extends BaseIntegrationTest {

    @Test
    void authorizeReturnsGithubUrl() throws Exception {
        String token = registerAndLogin(uniqueEmail("gh-authorize"));

        mockMvc.perform(get("/github/authorize").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorizeUrl").value(org.hamcrest.Matchers.containsString("github.com/login/oauth/authorize")));
    }

    @Test
    void accountReturnsDisconnectedByDefault() throws Exception {
        String token = registerAndLogin(uniqueEmail("gh-account"));

        mockMvc.perform(get("/github/account").header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.repos.length()").value(0));
    }

    @Test
    void syncFailsWhenNotConnected() throws Exception {
        String token = registerAndLogin(uniqueEmail("gh-sync"));

        mockMvc.perform(post("/github/sync").header("Authorization", authHeader(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void callbackRedirectsToErrorWhenParamsMissing() throws Exception {
        mockMvc.perform(get("/github/callback"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/settings?github=error")));
    }
}
