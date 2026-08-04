package com.devhub.goals.dto;

import com.devhub.goals.HabitFrequency;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class HabitRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private UUID goalId;
    private HabitFrequency frequency;
}
