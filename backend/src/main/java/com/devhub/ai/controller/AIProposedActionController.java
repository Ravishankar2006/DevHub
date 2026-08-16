package com.devhub.ai.controller;

import com.devhub.ai.AIProposedActionService;
import com.devhub.ai.dto.AIProposedActionDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ai/proposals")
@RequiredArgsConstructor
public class AIProposedActionController {

    private final AIProposedActionService aiProposedActionService;

    @PostMapping("/{id}/confirm")
    public ResponseEntity<AIProposedActionDto> confirm(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(aiProposedActionService.confirm(currentUser, id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AIProposedActionDto> reject(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(aiProposedActionService.reject(currentUser, id));
    }
}
