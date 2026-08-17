package com.devhub.projects.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
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
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<ProjectDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProjectRequest request) {
        ProjectDto created = projectService.createProject(currentUser, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "CREATE_PROJECT",
                created.getId().toString(), "Created project \"" + created.getName() + "\"");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
        ProjectDto updated = projectService.updateProject(currentUser, id, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "UPDATE_PROJECT",
                updated.getId().toString(), "Updated project \"" + updated.getName() + "\"");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        ProjectDto existing = projectService.getProject(currentUser, id);
        projectService.deleteProject(currentUser, id);
        activityLogService.log(currentUser, ActivityLogSource.USER, "DELETE_PROJECT",
                existing.getId().toString(), "Deleted project \"" + existing.getName() + "\"");
        return ResponseEntity.noContent().build();
    }
}
