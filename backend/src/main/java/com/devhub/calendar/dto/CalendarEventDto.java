package com.devhub.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDto {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private Instant startTime;
    private Instant endTime;
    private boolean allDay;
    private String location;
    private Instant createdAt;
    private Instant updatedAt;
}
