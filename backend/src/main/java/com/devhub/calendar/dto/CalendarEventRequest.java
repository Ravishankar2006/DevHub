package com.devhub.calendar.dto;

import com.devhub.calendar.CalendarEventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CalendarEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private CalendarEventCategory category;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    private Instant endTime;
    private boolean allDay;
    private String location;
}
