package com.devhub.calendar.controller;

import com.devhub.calendar.CalendarEventService;
import com.devhub.calendar.dto.CalendarEventDto;
import com.devhub.calendar.dto.CalendarEventRequest;
import com.devhub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calendar/events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @PostMapping
    public ResponseEntity<CalendarEventDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CalendarEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarEventService.createEvent(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<CalendarEventDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(calendarEventService.listEvents(currentUser, from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEventDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(calendarEventService.getEvent(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEventDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody CalendarEventRequest request) {
        return ResponseEntity.ok(calendarEventService.updateEvent(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        calendarEventService.deleteEvent(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
