package com.devhub.jobs;

import com.devhub.brief.DailyBriefService;
import com.devhub.documents.DocumentIndexService;
import com.devhub.github.GitHubAccountRepository;
import com.devhub.github.GitHubSyncService;
import com.devhub.resumes.ResumeReviewService;
import com.devhub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiJobScheduler {

    private static final long GITHUB_SYNC_INTERVAL_MS = 6 * 60 * 60 * 1000;

    private final AiJobRepository aiJobRepository;
    private final AiJobService aiJobService;
    private final ResumeReviewService resumeReviewService;
    private final DailyBriefService dailyBriefService;
    private final GitHubSyncService gitHubSyncService;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final UserRepository userRepository;
    private final DocumentIndexService documentIndexService;

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
                    case GITHUB_SYNC -> gitHubSyncService.performSync(job);
                    case DOCUMENT_INDEX -> documentIndexService.performIndexing(job);
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

    // Satisfies the PRD's "scheduled sync" requirement, distinct from manual resync.
    @Scheduled(fixedDelay = GITHUB_SYNC_INTERVAL_MS)
    public void scheduleGitHubSyncs() {
        List<UUID> userIds = gitHubAccountRepository.findAllUserIds();
        for (UUID userId : userIds) {
            userRepository.findById(userId)
                    .ifPresent(user -> aiJobService.createJob(user, AiJobType.GITHUB_SYNC, userId));
        }
    }
}
