package com.devhub.jobs.dto;

import com.devhub.jobs.AiJob;

public class AiJobMapper {

    public static AiJobDto toDto(AiJob job) {
        if (job == null) return null;

        return AiJobDto.builder()
                .id(job.getId())
                .jobType(job.getJobType().name())
                .status(job.getStatus().name())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
