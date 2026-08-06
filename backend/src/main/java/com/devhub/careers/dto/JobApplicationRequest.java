package com.devhub.careers.dto;

import com.devhub.careers.JobApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class JobApplicationRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String companyWebsite;

    @NotBlank(message = "Role title is required")
    private String roleTitle;

    private JobApplicationStatus status;
    private LocalDate appliedDate;
    private LocalDate deadline;
    private String location;
    private String jobPostingUrl;
    private String notes;
    private UUID resumeId;
}
