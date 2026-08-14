package com.devhub.common;

import java.time.LocalDate;
import java.util.Set;

/** Shared day-bucket streak algorithm, used wherever activity is tracked as a set of active epoch-days. */
public class StreakCalculator {

    private StreakCalculator() {}

    public static long computeCurrentStreak(Set<Long> activeDays) {
        long today = LocalDate.now().toEpochDay();
        long cursor;
        if (activeDays.contains(today)) {
            cursor = today;
        } else if (activeDays.contains(today - 1)) {
            cursor = today - 1;
        } else {
            return 0;
        }

        long streak = 0;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor--;
        }
        return streak;
    }

    public static long computeLongestStreak(Set<Long> activeDays) {
        long longest = 0;
        for (Long day : activeDays) {
            if (activeDays.contains(day - 1)) continue; // not the start of a run
            long run = 1;
            long cursor = day + 1;
            while (activeDays.contains(cursor)) {
                run++;
                cursor++;
            }
            longest = Math.max(longest, run);
        }
        return longest;
    }
}
