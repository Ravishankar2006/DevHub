package com.devhub.tasks.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
import com.devhub.tasks.TaskService;
import com.devhub.tasks.TaskStatus;
import com.devhub.tasks.dto.TaskDto;
import com.devhub.tasks.dto.TaskRequest;
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
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<TaskDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TaskRequest request) {
        TaskDto created = taskService.createTask(currentUser, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "CREATE_TASK",
                created.getId().toString(), "Created task \"" + created.getTitle() + "\"");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(taskService.listTasks(currentUser, projectId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTask(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request) {
        TaskDto updated = taskService.updateTask(currentUser, id, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "UPDATE_TASK",
                updated.getId().toString(), "Updated task \"" + updated.getTitle() + "\"");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        TaskDto existing = taskService.getTask(currentUser, id);
        taskService.deleteTask(currentUser, id);
        activityLogService.log(currentUser, ActivityLogSource.USER, "DELETE_TASK",
                existing.getId().toString(), "Deleted task \"" + existing.getTitle() + "\"");
        return ResponseEntity.noContent().build();
    }
}
