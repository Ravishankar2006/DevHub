package com.devhub.careers.dto;

import com.devhub.careers.JobApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobStatusChangeRequest {

    @NotNull(message = "Status is required")
    private JobApplicationStatus status;

    private String note;
}
