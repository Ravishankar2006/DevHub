package com.devhub.calendar.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
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
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<CalendarEventDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CalendarEventRequest request) {
        CalendarEventDto created = calendarEventService.createEvent(currentUser, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "CREATE_CALENDAR_EVENT",
                created.getId().toString(), "Created calendar event \"" + created.getTitle() + "\"");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
        CalendarEventDto updated = calendarEventService.updateEvent(currentUser, id, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "UPDATE_CALENDAR_EVENT",
                updated.getId().toString(), "Updated calendar event \"" + updated.getTitle() + "\"");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        CalendarEventDto existing = calendarEventService.getEvent(currentUser, id);
        calendarEventService.deleteEvent(currentUser, id);
        activityLogService.log(currentUser, ActivityLogSource.USER, "DELETE_CALENDAR_EVENT",
                existing.getId().toString(), "Deleted calendar event \"" + existing.getTitle() + "\"");
        return ResponseEntity.noContent().build();
    }
}
