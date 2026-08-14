package com.devhub.leetcode.controller;

import com.devhub.jobs.dto.AiJobDto;
import com.devhub.leetcode.LeetCodeService;
import com.devhub.leetcode.dto.LeetCodeAccountDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/leetcode")
@RequiredArgsConstructor
public class LeetCodeController {

    private final LeetCodeService leetCodeService;

    @GetMapping("/account")
    public ResponseEntity<LeetCodeAccountDto> account(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leetCodeService.getAccountStatus(currentUser));
    }

    @PostMapping("/connect")
    public ResponseEntity<LeetCodeAccountDto> connect(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(leetCodeService.connect(currentUser, body.get("username")));
    }

    @PostMapping("/sync")
    public ResponseEntity<AiJobDto> sync(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(leetCodeService.triggerManualSync(currentUser));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> disconnect(@AuthenticationPrincipal User currentUser) {
        leetCodeService.disconnect(currentUser);
        return ResponseEntity.noContent().build();
    }
}
