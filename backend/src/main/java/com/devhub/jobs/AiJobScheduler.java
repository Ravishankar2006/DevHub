package com.devhub.jobs;

import com.devhub.brief.DailyBriefService;
import com.devhub.resumes.ResumeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiJobScheduler {

    private final AiJobRepository aiJobRepository;
    private final ResumeReviewService resumeReviewService;
    private final DailyBriefService dailyBriefService;

    // Deliberately not @Transactional at this level: each repository save()
    // and each performReview() call already runs in its own transaction, so
    // a failed job's rollback can never mark a later "job = FAILED" write as
    // rollback-only. Wrapping the whole sweep in one transaction would let
    // that happen -- the FAILED status write would join the doomed
    // transaction and silently never commit.
    @Scheduled(fixedDelay = 5000)
    public void processPendingJobs() {
        List<AiJob> pendingJobs = aiJobRepository.findByStatus(AiJobStatus.PENDING);

        for (AiJob job : pendingJobs) {
            job.setStatus(AiJobStatus.PROCESSING);
            aiJobRepository.save(job);

            try {
                switch (job.getJobType()) {
                    case RESUME_REVIEW -> resumeReviewService.performReview(job);
                    case DAILY_BRIEF -> dailyBriefService.performGeneration(job);
                }
                job.setStatus(AiJobStatus.COMPLETED);
            } catch (Exception e) {
                log.warn("AI job {} ({}) failed: {}", job.getId(), job.getJobType(), e.getMessage());
                job.setStatus(AiJobStatus.FAILED);
                job.setErrorMessage(e.getMessage());
            }

            aiJobRepository.save(job);
        }
    }
}
