package com.devhub.tasks.dto;

import com.devhub.tasks.TaskPriority;
import com.devhub.tasks.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private UUID projectId;
    private UUID milestoneId;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
}
