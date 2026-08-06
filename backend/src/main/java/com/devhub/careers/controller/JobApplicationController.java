package com.devhub.careers.controller;

import com.devhub.careers.JobApplicationService;
import com.devhub.careers.JobApplicationStatus;
import com.devhub.careers.dto.JobApplicationDto;
import com.devhub.careers.dto.JobApplicationRequest;
import com.devhub.careers.dto.JobStatusChangeRequest;
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
@RequestMapping("/careers/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    public ResponseEntity<JobApplicationDto> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplicationService.createApplication(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationDto>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) JobApplicationStatus status,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(jobApplicationService.listApplications(currentUser, status, companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationDto> get(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(jobApplicationService.getApplication(currentUser, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationDto> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.ok(jobApplicationService.updateApplication(currentUser, id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplicationDto> changeStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody JobStatusChangeRequest request) {
        return ResponseEntity.ok(jobApplicationService.changeStatus(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        jobApplicationService.deleteApplication(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
