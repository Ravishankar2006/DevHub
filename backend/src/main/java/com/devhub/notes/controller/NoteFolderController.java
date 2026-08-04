package com.devhub.notes.controller;

import com.devhub.notes.NoteService;
import com.devhub.notes.dto.NoteFolderDto;
import com.devhub.notes.dto.NoteFolderRequest;
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
@RequestMapping("/notes/folders")
@RequiredArgsConstructor
public class NoteFolderController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteFolderDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody NoteFolderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createFolder(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<NoteFolderDto>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(noteService.listFolders(currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteFolderDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody NoteFolderRequest request) {
        return ResponseEntity.ok(noteService.updateFolder(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        noteService.deleteFolder(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
