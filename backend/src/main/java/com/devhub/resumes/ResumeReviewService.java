package com.devhub.resumes;

import com.devhub.ai.GeminiChatClient;
import com.devhub.common.ApiException;
import com.devhub.jobs.AiJob;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.jobs.dto.AiJobMapper;
import com.devhub.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeReviewService {

    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;
    private final ResumeFileStorageService storageService;
    private final AiJobService aiJobService;
    private final GeminiChatClient geminiChatClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public AiJobDto triggerReview(User currentUser, UUID resumeId) {
        Resume resume = resumeService.getOwnedResume(currentUser, resumeId);
        AiJob job = aiJobService.createJob(currentUser, AiJobType.RESUME_REVIEW, resume.getId());
        return AiJobMapper.toDto(job);
    }

    @Transactional
    public void performReview(AiJob job) {
        Resume resume = resumeRepository.findById(job.getTargetId())
                .orElseThrow(() -> new ApiException("Resume no longer exists", HttpStatus.NOT_FOUND));

        Resource fileResource = storageService.loadAsResource(resume.getStoragePath());

        byte[] pdfBytes;
        try {
            pdfBytes = fileResource.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new ApiException("Could not read resume file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        GeminiChatClient.ResumeAnalysis analysis = geminiChatClient.analyzeResume(pdfBytes);

        try {
            resume.setReviewScore(analysis.score());
            resume.setReviewSummary(analysis.summary());
            resume.setReviewIssues(objectMapper.writeValueAsString(analysis.issues()));
            resume.setReviewSuggestions(objectMapper.writeValueAsString(analysis.suggestions()));
            resume.setReviewedAt(Instant.now());
        } catch (Exception e) {
            throw new ApiException("Failed to store review result", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        resumeRepository.save(resume);
        log.info("Resume review completed for resume {}", resume.getId());
    }
}
