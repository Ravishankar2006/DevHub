package com.devhub.goals.controller;

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

    @PostMapping
    public ResponseEntity<HabitDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody HabitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.createHabit(currentUser, request));
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
        return ResponseEntity.ok(habitService.updateHabit(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        habitService.deleteHabit(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/checkins/today")
    public ResponseEntity<HabitDto> toggleTodayCheckin(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(habitService.toggleTodayCheckin(currentUser, id));
    }
}
