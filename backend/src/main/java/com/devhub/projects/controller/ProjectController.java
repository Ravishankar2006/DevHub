package com.devhub.projects.controller;

import com.devhub.projects.ProjectService;
import com.devhub.projects.dto.ProjectDto;
import com.devhub.projects.dto.ProjectRequest;
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
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        return ResponseEntity.ok(projectService.listProjects(currentUser, includeArchived));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        projectService.deleteProject(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
