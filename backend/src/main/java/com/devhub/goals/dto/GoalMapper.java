package com.devhub.goals.dto;

import com.devhub.goals.Goal;

public class GoalMapper {

    public static GoalDto toDto(Goal goal) {
        if (goal == null) return null;

        return GoalDto.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .type(goal.getType().name())
                .status(goal.getStatus().name())
                .targetDate(goal.getTargetDate())
                .progressPercent(goal.getProgressPercent())
                .projectId(goal.getProject() != null ? goal.getProject().getId() : null)
                .projectName(goal.getProject() != null ? goal.getProject().getName() : null)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
