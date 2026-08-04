package com.devhub.projects.dto;

import com.devhub.projects.Milestone;

public class MilestoneMapper {

    public static MilestoneDto toDto(Milestone milestone, long totalTasks, long completedTasks) {
        if (milestone == null) return null;

        int progressPercent = totalTasks == 0 ? 0 : (int) Math.round((completedTasks * 100.0) / totalTasks);

        return MilestoneDto.builder()
                .id(milestone.getId())
                .projectId(milestone.getProject().getId())
                .projectName(milestone.getProject().getName())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .dueDate(milestone.getDueDate())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercent(progressPercent)
                .createdAt(milestone.getCreatedAt())
                .updatedAt(milestone.getUpdatedAt())
                .build();
    }
}
