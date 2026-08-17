package com.devhub.activity.dto;

import com.devhub.activity.ActivityLog;

public class ActivityLogMapper {

    public static ActivityLogDto toDto(ActivityLog log) {
        if (log == null) return null;

        return ActivityLogDto.builder()
                .id(log.getId())
                .source(log.getSource().name())
                .actionType(log.getActionType())
                .targetEntityRef(log.getTargetEntityRef())
                .summary(log.getSummary())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
