package com.devhub.github;

import com.devhub.auth.JwtTokenProvider;
import com.devhub.common.ApiException;
import com.devhub.github.dto.GitHubAccountDto;
import com.devhub.github.dto.GitHubMapper;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.users.User;
import com.devhub.users.UserRepository;
import com.devhub.users.UserSettings;
import com.devhub.users.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class GitHubOAuthService {

    private final GitHubAccountRepository gitHubAccountRepository;
    private final GitHubRepoRepository gitHubRepoRepository;
    private final GitHubApiClient gitHubApiClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final AiJobService aiJobService;
    private final String clientId;
    private final String redirectUri;
    private final String frontendUrl;

    public GitHubOAuthService(
            GitHubAccountRepository gitHubAccountRepository,
            GitHubRepoRepository gitHubRepoRepository,
            GitHubApiClient gitHubApiClient,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            AiJobService aiJobService,
            @Value("${devhub.github.client-id}") String clientId,
            @Value("${devhub.github.redirect-uri}") String redirectUri,
            @Value("${devhub.frontend-url}") String frontendUrl) {
        this.gitHubAccountRepository = gitHubAccountRepository;
        this.gitHubRepoRepository = gitHubRepoRepository;
        this.gitHubApiClient = gitHubApiClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.aiJobService = aiJobService;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.frontendUrl = frontendUrl;
    }

    public String buildAuthorizeUrl(User currentUser) {
        String state = jwtTokenProvider.generateAccessToken(currentUser.getEmail());
        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("read:user repo", StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    public String successRedirectUrl() {
        return frontendUrl + "/settings?github=connected";
    }

    public String errorRedirectUrl() {
        return frontendUrl + "/settings?github=error";
    }

    @Transactional
    public void handleCallback(String code, String state) {
        if (!jwtTokenProvider.validateToken(state)) {
            throw new ApiException("Invalid or expired authorization session.", HttpStatus.UNAUTHORIZED);
        }
        String email = jwtTokenProvider.getEmailFromToken(state);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        String accessToken = gitHubApiClient.exchangeCodeForToken(code);
        GitHubApiClient.GitHubProfile profile = gitHubApiClient.fetchProfile(accessToken);

        GitHubAccount account = gitHubAccountRepository.findByUserId(user.getId())
                .orElseGet(() -> GitHubAccount.builder().user(user).build());
        account.setGithubUserId(profile.id());
        account.setGithubUsername(profile.login());
        account.setAvatarUrl(profile.avatarUrl());
        account.setAccessToken(accessToken);
        if (account.getConnectedAt() == null) {
            account.setConnectedAt(Instant.now());
        }
        gitHubAccountRepository.save(account);

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));
        settings.setGithubConnected(true);
        userSettingsRepository.save(settings);

        aiJobService.createJob(user, AiJobType.GITHUB_SYNC, user.getId());
    }

    @Transactional(readOnly = true)
    public GitHubAccountDto getAccountStatus(User currentUser) {
        return gitHubAccountRepository.findByUserId(currentUser.getId())
                .map(account -> GitHubMapper.toDto(
                        account, gitHubRepoRepository.findByGithubAccountIdOrderByPushedAtDesc(account.getId())))
                .orElseGet(GitHubMapper::disconnected);
    }

    @Transactional
    public void disconnect(User currentUser) {
        GitHubAccount account = gitHubAccountRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ApiException("GitHub account is not connected.", HttpStatus.NOT_FOUND));

        gitHubRepoRepository.deleteByGithubAccountId(account.getId());
        gitHubAccountRepository.delete(account);

        UserSettings settings = userSettingsRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));
        settings.setGithubConnected(false);
        userSettingsRepository.save(settings);
    }
}
