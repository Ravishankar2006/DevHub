package com.devhub.documents.controller;

import com.devhub.documents.DocumentUploadService;
import com.devhub.documents.dto.DocumentDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentUploadService documentUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> upload(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentUploadService.uploadDocument(currentUser, file, title));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(documentUploadService.listDocuments(currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        documentUploadService.deleteDocument(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
