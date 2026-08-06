package com.devhub.careers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDto {
    private UUID id;
    private String roleTitle;
    private String status;
    private LocalDate appliedDate;
    private LocalDate deadline;
    private String location;
    private String jobPostingUrl;
    private String notes;
    private CompanyDto company;
    private UUID resumeId;
    private String resumeName;
    private List<JobStatusHistoryDto> statusHistory;
    private Instant createdAt;
    private Instant updatedAt;
}
