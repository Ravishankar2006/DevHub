package com.devhub.careers;

import com.devhub.careers.dto.JobApplicationDto;
import com.devhub.careers.dto.JobApplicationMapper;
import com.devhub.careers.dto.JobApplicationRequest;
import com.devhub.careers.dto.JobStatusChangeRequest;
import com.devhub.common.ApiException;
import com.devhub.resumes.Resume;
import com.devhub.resumes.ResumeRepository;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobStatusHistoryRepository jobStatusHistoryRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyService companyService;

    @Transactional
    public JobApplicationDto createApplication(User currentUser, JobApplicationRequest request) {
        Company company = companyService.findOrCreate(currentUser, request.getCompanyName(), request.getCompanyWebsite());
        Resume resume = resolveResume(currentUser, request.getResumeId());

        JobApplicationStatus status = request.getStatus() != null ? request.getStatus() : JobApplicationStatus.WISHLIST;

        JobApplication application = JobApplication.builder()
                .user(currentUser)
                .company(company)
                .resume(resume)
                .roleTitle(request.getRoleTitle())
                .status(status)
                .appliedDate(request.getAppliedDate())
                .deadline(request.getDeadline())
                .location(request.getLocation())
                .jobPostingUrl(request.getJobPostingUrl())
                .notes(request.getNotes())
                .build();

        application = jobApplicationRepository.save(application);
        appendHistory(application, status, null);

        return toDto(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDto> listApplications(User currentUser, JobApplicationStatus status, UUID companyId) {
        UUID userId = currentUser.getId();
        List<JobApplication> applications;
        if (status != null && companyId != null) {
            applications = jobApplicationRepository.findByUserIdAndStatusAndCompanyIdOrderByUpdatedAtDesc(userId, status, companyId);
        } else if (status != null) {
            applications = jobApplicationRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, status);
        } else if (companyId != null) {
            applications = jobApplicationRepository.findByUserIdAndCompanyIdOrderByUpdatedAtDesc(userId, companyId);
        } else {
            applications = jobApplicationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        }
        return applications.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobApplicationDto getApplication(User currentUser, UUID applicationId) {
        return toDto(getOwnedApplication(currentUser, applicationId));
    }

    @Transactional
    public JobApplicationDto updateApplication(User currentUser, UUID applicationId, JobApplicationRequest request) {
        JobApplication application = getOwnedApplication(currentUser, applicationId);
        Company company = companyService.findOrCreate(currentUser, request.getCompanyName(), request.getCompanyWebsite());
        Resume resume = resolveResume(currentUser, request.getResumeId());

        JobApplicationStatus newStatus = request.getStatus() != null ? request.getStatus() : application.getStatus();
        boolean statusChanged = newStatus != application.getStatus();

        application.setCompany(company);
        application.setResume(resume);
        application.setRoleTitle(request.getRoleTitle());
        application.setStatus(newStatus);
        application.setAppliedDate(request.getAppliedDate());
        application.setDeadline(request.getDeadline());
        application.setLocation(request.getLocation());
        application.setJobPostingUrl(request.getJobPostingUrl());
        application.setNotes(request.getNotes());

        application = jobApplicationRepository.save(application);

        if (statusChanged) {
            appendHistory(application, newStatus, null);
        }

        return toDto(application);
    }

    @Transactional
    public JobApplicationDto changeStatus(User currentUser, UUID applicationId, JobStatusChangeRequest request) {
        JobApplication application = getOwnedApplication(currentUser, applicationId);

        application.setStatus(request.getStatus());
        application = jobApplicationRepository.save(application);
        appendHistory(application, request.getStatus(), request.getNote());

        return toDto(application);
    }

    @Transactional
    public void deleteApplication(User currentUser, UUID applicationId) {
        JobApplication application = getOwnedApplication(currentUser, applicationId);
        jobStatusHistoryRepository.deleteByJobApplicationId(applicationId);
        jobApplicationRepository.delete(application);
    }

    JobApplication getOwnedApplication(User currentUser, UUID applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException("Job application not found", HttpStatus.NOT_FOUND));

        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Job application not found", HttpStatus.NOT_FOUND);
        }
        return application;
    }

    private Resume resolveResume(User currentUser, UUID resumeId) {
        if (resumeId == null) return null;

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Resume not found", HttpStatus.NOT_FOUND);
        }
        return resume;
    }

    private void appendHistory(JobApplication application, JobApplicationStatus status, String note) {
        jobStatusHistoryRepository.save(
                JobStatusHistory.builder()
                        .jobApplication(application)
                        .status(status)
                        .note(note)
                        .changedAt(Instant.now())
                        .build());
    }

    private JobApplicationDto toDto(JobApplication application) {
        List<JobStatusHistory> history = jobStatusHistoryRepository.findByJobApplicationIdOrderByChangedAtDesc(application.getId());
        return JobApplicationMapper.toDto(application, history);
    }
}
