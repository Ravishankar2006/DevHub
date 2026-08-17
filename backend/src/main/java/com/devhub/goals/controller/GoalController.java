package com.devhub.goals.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
import com.devhub.goals.GoalService;
import com.devhub.goals.GoalStatus;
import com.devhub.goals.dto.GoalDto;
import com.devhub.goals.dto.GoalRequest;
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
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<GoalDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody GoalRequest request) {
        GoalDto created = goalService.createGoal(currentUser, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "CREATE_GOAL",
                created.getId().toString(), "Created goal \"" + created.getTitle() + "\"");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<GoalDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) GoalStatus status) {
        return ResponseEntity.ok(goalService.listGoals(currentUser, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(goalService.getGoal(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody GoalRequest request) {
        GoalDto updated = goalService.updateGoal(currentUser, id, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "UPDATE_GOAL",
                updated.getId().toString(), "Updated goal \"" + updated.getTitle() + "\"");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        GoalDto existing = goalService.getGoal(currentUser, id);
        goalService.deleteGoal(currentUser, id);
        activityLogService.log(currentUser, ActivityLogSource.USER, "DELETE_GOAL",
                existing.getId().toString(), "Deleted goal \"" + existing.getTitle() + "\"");
        return ResponseEntity.noContent().build();
    }
}
