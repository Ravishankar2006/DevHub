package com.devhub.goals.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
import com.devhub.goals.HabitService;
import com.devhub.goals.dto.HabitDto;
import com.devhub.goals.dto.HabitRequest;
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
@RequestMapping("/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<HabitDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody HabitRequest request) {
        HabitDto created = habitService.createHabit(currentUser, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "CREATE_HABIT",
                created.getId().toString(), "Created habit \"" + created.getTitle() + "\"");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<HabitDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID goalId) {
        return ResponseEntity.ok(habitService.listHabits(currentUser, goalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(habitService.getHabit(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody HabitRequest request) {
        HabitDto updated = habitService.updateHabit(currentUser, id, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "UPDATE_HABIT",
                updated.getId().toString(), "Updated habit \"" + updated.getTitle() + "\"");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        HabitDto existing = habitService.getHabit(currentUser, id);
        habitService.deleteHabit(currentUser, id);
        activityLogService.log(currentUser, ActivityLogSource.USER, "DELETE_HABIT",
                existing.getId().toString(), "Deleted habit \"" + existing.getTitle() + "\"");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/checkins/today")
    public ResponseEntity<HabitDto> toggleTodayCheckin(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(habitService.toggleTodayCheckin(currentUser, id));
    }
}
