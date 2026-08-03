package com.devhub.tasks.dto;

import com.devhub.tasks.Task;

public class TaskMapper {

    public static TaskDto toDto(Task task) {
        if (task == null) return null;

        return TaskDto.builder()
                .id(task.getId())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .milestoneId(task.getMilestone() != null ? task.getMilestone().getId() : null)
                .milestoneTitle(task.getMilestone() != null ? task.getMilestone().getTitle() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .position(task.getPosition())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
