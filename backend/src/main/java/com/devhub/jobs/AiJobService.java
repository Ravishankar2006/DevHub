package com.devhub.jobs;

import com.devhub.common.ApiException;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiJobService {

    private final AiJobRepository aiJobRepository;

    @Transactional
    public AiJob createJob(User currentUser, AiJobType jobType, UUID targetId) {
        return aiJobRepository.save(
                AiJob.builder()
                        .user(currentUser)
                        .jobType(jobType)
                        .targetId(targetId)
                        .build());
    }

    @Transactional(readOnly = true)
    public AiJob getOwnedJob(User currentUser, UUID jobId) {
        AiJob job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));

        if (!job.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Job not found", HttpStatus.NOT_FOUND);
        }
        return job;
    }
}
