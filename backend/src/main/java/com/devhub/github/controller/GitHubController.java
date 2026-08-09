package com.devhub.github.controller;

import com.devhub.github.GitHubOAuthService;
import com.devhub.github.GitHubSyncService;
import com.devhub.github.dto.GitHubAccountDto;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubOAuthService gitHubOAuthService;
    private final GitHubSyncService gitHubSyncService;

    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(Map.of("authorizeUrl", gitHubOAuthService.buildAuthorizeUrl(currentUser)));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        String redirectTarget;
        if (code == null || state == null) {
            redirectTarget = gitHubOAuthService.errorRedirectUrl();
        } else {
            try {
                gitHubOAuthService.handleCallback(code, state);
                redirectTarget = gitHubOAuthService.successRedirectUrl();
            } catch (Exception e) {
                log.warn("GitHub OAuth callback failed: {}", e.getMessage());
                redirectTarget = gitHubOAuthService.errorRedirectUrl();
            }
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectTarget)).build();
    }

    @GetMapping("/account")
    public ResponseEntity<GitHubAccountDto> account(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(gitHubOAuthService.getAccountStatus(currentUser));
    }

    @PostMapping("/sync")
    public ResponseEntity<AiJobDto> sync(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(gitHubSyncService.triggerManualSync(currentUser));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> disconnect(@AuthenticationPrincipal User currentUser) {
        gitHubOAuthService.disconnect(currentUser);
        return ResponseEntity.noContent().build();
    }
}
