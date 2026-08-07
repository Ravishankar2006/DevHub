package com.devhub.ai.controller;

import com.devhub.ai.AIConversationService;
import com.devhub.ai.dto.AIConversationDetailDto;
import com.devhub.ai.dto.AIConversationSummaryDto;
import com.devhub.ai.dto.SendMessageRequest;
import com.devhub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai/conversations")
@RequiredArgsConstructor
public class AIConversationController {

    private final AIConversationService aiConversationService;

    @PostMapping
    public ResponseEntity<AIConversationDetailDto> create(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiConversationService.createConversation(currentUser));
    }

    @GetMapping
    public ResponseEntity<List<AIConversationSummaryDto>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(aiConversationService.listConversations(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AIConversationDetailDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(aiConversationService.getConversation(currentUser, id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AIConversationDetailDto> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(aiConversationService.sendMessage(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        aiConversationService.deleteConversation(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
