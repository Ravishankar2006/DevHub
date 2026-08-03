package com.devhub.projects.controller;

import com.devhub.projects.MilestoneService;
import com.devhub.projects.dto.MilestoneDto;
import com.devhub.projects.dto.MilestoneRequest;
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
@RequestMapping("/projects/{projectId}/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    public ResponseEntity<MilestoneDto> create(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID projectId,
            @Valid @RequestBody MilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(milestoneService.createMilestone(currentUser, projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<MilestoneDto>> list(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(milestoneService.listMilestones(currentUser, projectId));
    }

    @PutMapping("/{milestoneId}")
    public ResponseEntity<MilestoneDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID projectId,
            @PathVariable UUID milestoneId,
            @Valid @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(milestoneService.updateMilestone(currentUser, projectId, milestoneId, request));
    }

    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID projectId,
            @PathVariable UUID milestoneId) {
        milestoneService.deleteMilestone(currentUser, projectId, milestoneId);
        return ResponseEntity.noContent().build();
    }
}
