package com.devhub.goals.dto;

import com.devhub.goals.Habit;

public class HabitMapper {

    public static HabitDto toDto(Habit habit, int currentStreak, int longestStreak, boolean checkedInToday) {
        if (habit == null) return null;

        return HabitDto.builder()
                .id(habit.getId())
                .goalId(habit.getGoal() != null ? habit.getGoal().getId() : null)
                .goalTitle(habit.getGoal() != null ? habit.getGoal().getTitle() : null)
                .title(habit.getTitle())
                .frequency(habit.getFrequency().name())
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .checkedInToday(checkedInToday)
                .createdAt(habit.getCreatedAt())
                .updatedAt(habit.getUpdatedAt())
                .build();
    }
}
