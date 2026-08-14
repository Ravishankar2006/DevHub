package com.devhub.leetcode;

import com.devhub.common.ApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * LeetCode has no official public API or OAuth app registration; this uses the same public
 * GraphQL endpoint leetcode.com itself calls to render a user's public profile page. No
 * authentication is required or possible -- a username is enough to look up public stats.
 */
@Component
public class LeetCodeApiClient {

    private static final String PROFILE_QUERY = """
            query getUserProfile($username: String!) {
              matchedUser(username: $username) {
                username
                profile { ranking }
                submitStatsGlobal { acSubmissionNum { difficulty count submissions } }
                userCalendar { submissionCalendar }
              }
            }
            """;

    private final RestClient apiClient;
    private final RetryTemplate retryTemplate;

    public LeetCodeApiClient(RetryTemplate externalCallRetryTemplate) {
        this.retryTemplate = externalCallRetryTemplate;
        this.apiClient = RestClient.builder()
                .baseUrl("https://leetcode.com")
                .defaultHeader("Referer", "https://leetcode.com")
                .build();
    }

    /** Returns null if the username doesn't match a real LeetCode account. */
    public MatchedUser fetchProfile(String username) {
        try {
            GraphQLResponse response = retryTemplate.execute(ctx -> apiClient.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new QueryRequest(PROFILE_QUERY, Map.of("username", username)))
                    .retrieve()
                    .body(GraphQLResponse.class));
            return response == null || response.data() == null ? null : response.data().matchedUser();
        } catch (RestClientException e) {
            throw new ApiException("Could not reach LeetCode to fetch profile stats.", HttpStatus.BAD_GATEWAY);
        }
    }

    private record QueryRequest(String query, Map<String, String> variables) {}

    private record GraphQLResponse(GraphQLData data) {}

    private record GraphQLData(MatchedUser matchedUser) {}

    public record MatchedUser(
            String username,
            Profile profile,
            @JsonProperty("submitStatsGlobal") SubmitStats submitStats,
            @JsonProperty("userCalendar") UserCalendar userCalendar) {}

    public record Profile(Long ranking) {}

    public record SubmitStats(@JsonProperty("acSubmissionNum") List<AcSubmission> acSubmissionNum) {}

    public record AcSubmission(String difficulty, Integer count, Integer submissions) {}

    /** submissionCalendar is itself a JSON-encoded string: {"<unix-seconds-utc-midnight>": <count>, ...} */
    public record UserCalendar(String submissionCalendar) {}
}
