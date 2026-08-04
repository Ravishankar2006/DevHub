package com.devhub.goals.controller;

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

    @PostMapping
    public ResponseEntity<GoalDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(currentUser, request));
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
        return ResponseEntity.ok(goalService.updateGoal(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        goalService.deleteGoal(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
