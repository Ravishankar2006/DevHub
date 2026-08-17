package com.devhub.notes.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.ActivityLogSource;
import com.devhub.notes.NoteService;
import com.devhub.notes.dto.NoteDto;
import com.devhub.notes.dto.NoteRequest;
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
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<NoteDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody NoteRequest request) {
        NoteDto created = noteService.createNote(currentUser, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "CREATE_NOTE",
                created.getId().toString(), "Created note \"" + created.getTitle() + "\"");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NoteDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(noteService.listNotes(currentUser, folderId, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(noteService.getNote(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody NoteRequest request) {
        NoteDto updated = noteService.updateNote(currentUser, id, request);
        activityLogService.log(currentUser, ActivityLogSource.USER, "UPDATE_NOTE",
                updated.getId().toString(), "Updated note \"" + updated.getTitle() + "\"");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        NoteDto existing = noteService.getNote(currentUser, id);
        noteService.deleteNote(currentUser, id);
        activityLogService.log(currentUser, ActivityLogSource.USER, "DELETE_NOTE",
                existing.getId().toString(), "Deleted note \"" + existing.getTitle() + "\"");
        return ResponseEntity.noContent().build();
    }
}
