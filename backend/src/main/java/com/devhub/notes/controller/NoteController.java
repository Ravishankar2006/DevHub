package com.devhub.notes.controller;

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

    @PostMapping
    public ResponseEntity<NoteDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(currentUser, request));
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
        return ResponseEntity.ok(noteService.updateNote(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        noteService.deleteNote(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
