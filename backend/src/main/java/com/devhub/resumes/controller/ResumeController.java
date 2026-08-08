package com.devhub.resumes.controller;

import com.devhub.jobs.dto.AiJobDto;
import com.devhub.resumes.Resume;
import com.devhub.resumes.ResumeFileStorageService;
import com.devhub.resumes.ResumeReviewService;
import com.devhub.resumes.ResumeService;
import com.devhub.resumes.dto.ResumeDto;
import com.devhub.resumes.dto.ResumeMetadataRequest;
import com.devhub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeFileStorageService storageService;
    private final ResumeReviewService resumeReviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeDto> create(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.createResume(currentUser, file, name, label, notes));
    }

    @GetMapping
    public ResponseEntity<List<ResumeDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String label) {
        return ResponseEntity.ok(resumeService.listResumes(currentUser, label));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(resumeService.getResume(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ResumeMetadataRequest request) {
        return ResponseEntity.ok(resumeService.updateResume(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        resumeService.deleteResume(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<AiJobDto> triggerReview(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(resumeReviewService.triggerReview(currentUser, id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        Resume resume = resumeService.downloadResume(currentUser, id);
        Resource fileResource = storageService.loadAsResource(resume.getStoragePath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(resume.getFileName()).build().toString())
                .body(fileResource);
    }
}
