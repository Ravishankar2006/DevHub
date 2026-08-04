package com.devhub.goals.dto;

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
public class HabitDto {
    private UUID id;
    private UUID goalId;
    private String goalTitle;
    private String title;
    private String frequency;
    private int currentStreak;
    private int longestStreak;
    private boolean checkedInToday;
    private Instant createdAt;
    private Instant updatedAt;
}
