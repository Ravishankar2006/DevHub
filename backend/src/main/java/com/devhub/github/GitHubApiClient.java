package com.devhub.github;

import com.devhub.common.ApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class GitHubApiClient {

    private final RestClient oauthClient;
    private final RestClient apiClient;
    private final RetryTemplate retryTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GitHubApiClient(
            @Value("${devhub.github.client-id}") String clientId,
            @Value("${devhub.github.client-secret}") String clientSecret,
            @Value("${devhub.github.redirect-uri}") String redirectUri,
            RetryTemplate externalCallRetryTemplate) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.retryTemplate = externalCallRetryTemplate;
        this.oauthClient = RestClient.builder().baseUrl("https://github.com").build();
        this.apiClient = RestClient.builder().baseUrl("https://api.github.com").build();
    }

    public String exchangeCodeForToken(String code) {
        try {
            TokenResponse response = retryTemplate.execute(ctx -> oauthClient.post()
                    .uri("/login/oauth/access_token")
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TokenRequest(clientId, clientSecret, code, redirectUri))
                    .retrieve()
                    .body(TokenResponse.class));

            if (response == null || response.accessToken() == null) {
                throw new ApiException("GitHub did not return an access token.", HttpStatus.BAD_GATEWAY);
            }
            return response.accessToken();
        } catch (RestClientException e) {
            throw new ApiException("Could not reach GitHub to complete authorization.", HttpStatus.BAD_GATEWAY);
        }
    }

    public GitHubProfile fetchProfile(String accessToken) {
        try {
            GitHubProfile profile = retryTemplate.execute(ctx -> apiClient.get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(GitHubProfile.class));

            if (profile == null) {
                throw new ApiException("GitHub did not return a profile.", HttpStatus.BAD_GATEWAY);
            }
            return profile;
        } catch (RestClientException e) {
            throw new ApiException("Could not reach GitHub to fetch the account profile.", HttpStatus.BAD_GATEWAY);
        }
    }

    public List<GitHubRepoPayload> fetchRepos(String accessToken) {
        try {
            List<GitHubRepoPayload> repos = retryTemplate.execute(ctx -> apiClient.get()
                    .uri("/user/repos?per_page=100&sort=pushed&affiliation=owner")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepoPayload>>() {}));

            return repos == null ? List.of() : repos;
        } catch (RestClientException e) {
            throw new ApiException("Could not reach GitHub to sync repositories.", HttpStatus.BAD_GATEWAY);
        }
    }

    /** Best-effort: a single repo's languages failing to resolve shouldn't fail the whole sync. */
    public Map<String, Integer> fetchLanguages(String accessToken, String fullName) {
        try {
            Map<String, Integer> languages = retryTemplate.execute(ctx -> apiClient.get()
                    .uri("/repos/" + fullName + "/languages")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Integer>>() {}));
            return languages == null ? Map.of() : languages;
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /**
     * Public + (since we're authenticated as the account owner) private events for the user,
     * up to GitHub's own retention window (~90 days, ~300 events).
     */
    public List<GitHubEventPayload> fetchEvents(String accessToken, String username) {
        try {
            List<GitHubEventPayload> all = new java.util.ArrayList<>();
            for (int page = 1; page <= 3; page++) {
                String uri = "/users/" + username + "/events?per_page=100&page=" + page;
                List<GitHubEventPayload> batch = retryTemplate.execute(ctx -> apiClient.get()
                        .uri(uri)
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/vnd.github+json")
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<GitHubEventPayload>>() {}));
                if (batch == null || batch.isEmpty()) break;
                all.addAll(batch);
                if (batch.size() < 100) break;
            }
            return all;
        } catch (RestClientException e) {
            throw new ApiException("Could not reach GitHub to sync activity.", HttpStatus.BAD_GATEWAY);
        }
    }

    private record TokenRequest(
            @JsonProperty("client_id") String clientId,
            @JsonProperty("client_secret") String clientSecret,
            String code,
            @JsonProperty("redirect_uri") String redirectUri) {}

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

    public record GitHubProfile(
            Long id,
            String login,
            @JsonProperty("avatar_url") String avatarUrl) {}

    public record GitHubRepoPayload(
            Long id,
            String name,
            @JsonProperty("full_name") String fullName,
            String description,
            String language,
            @JsonProperty("stargazers_count") Integer stargazersCount,
            @JsonProperty("forks_count") Integer forksCount,
            @JsonProperty("html_url") String htmlUrl,
            @JsonProperty("private") Boolean isPrivate,
            Boolean fork,
            @JsonProperty("open_issues_count") Integer openIssuesCount,
            @JsonProperty("watchers_count") Integer watchersCount,
            @JsonProperty("pushed_at") Instant pushedAt) {}

    public record GitHubEventPayload(
            String type,
            @JsonProperty("created_at") Instant createdAt,
            GitHubEventDetail payload) {}

    public record GitHubEventDetail(@JsonProperty("distinct_size") Integer distinctSize) {}
}
