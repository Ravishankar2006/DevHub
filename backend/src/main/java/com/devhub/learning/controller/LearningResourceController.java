package com.devhub.learning.controller;

import com.devhub.learning.LearningResourceService;
import com.devhub.learning.LearningResourceType;
import com.devhub.learning.LearningStatus;
import com.devhub.learning.dto.LearningResourceDto;
import com.devhub.learning.dto.LearningResourceRequest;
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
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    @PostMapping
    public ResponseEntity<LearningResourceDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody LearningResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(learningResourceService.createResource(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<LearningResourceDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) LearningStatus status,
            @RequestParam(required = false) LearningResourceType resourceType) {
        return ResponseEntity.ok(learningResourceService.listResources(currentUser, status, resourceType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningResourceDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(learningResourceService.getResource(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningResourceDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody LearningResourceRequest request) {
        return ResponseEntity.ok(learningResourceService.updateResource(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        learningResourceService.deleteResource(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
