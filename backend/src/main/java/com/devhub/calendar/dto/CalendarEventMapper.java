package com.devhub.calendar.dto;

import com.devhub.calendar.CalendarEvent;

public class CalendarEventMapper {

    public static CalendarEventDto toDto(CalendarEvent event) {
        if (event == null) return null;

        return CalendarEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory().name())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .allDay(event.isAllDay())
                .location(event.getLocation())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
