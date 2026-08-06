package com.devhub.careers.dto;

import com.devhub.careers.Company;
import com.devhub.careers.JobApplication;
import com.devhub.careers.JobStatusHistory;

import java.util.List;
import java.util.stream.Collectors;

public class JobApplicationMapper {

    public static CompanyDto toCompanyDto(Company company) {
        if (company == null) return null;

        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .website(company.getWebsite())
                .notes(company.getNotes())
                .build();
    }

    public static JobStatusHistoryDto toHistoryDto(JobStatusHistory history) {
        if (history == null) return null;

        return JobStatusHistoryDto.builder()
                .id(history.getId())
                .status(history.getStatus().name())
                .note(history.getNote())
                .changedAt(history.getChangedAt())
                .build();
    }

    public static JobApplicationDto toDto(JobApplication application, List<JobStatusHistory> history) {
        if (application == null) return null;

        List<JobStatusHistoryDto> historyDtos = history == null
                ? List.of()
                : history.stream().map(JobApplicationMapper::toHistoryDto).collect(Collectors.toList());

        return JobApplicationDto.builder()
                .id(application.getId())
                .roleTitle(application.getRoleTitle())
                .status(application.getStatus().name())
                .appliedDate(application.getAppliedDate())
                .deadline(application.getDeadline())
                .location(application.getLocation())
                .jobPostingUrl(application.getJobPostingUrl())
                .notes(application.getNotes())
                .company(toCompanyDto(application.getCompany()))
                .resumeId(application.getResume() != null ? application.getResume().getId() : null)
                .resumeName(application.getResume() != null ? application.getResume().getName() : null)
                .statusHistory(historyDtos)
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
