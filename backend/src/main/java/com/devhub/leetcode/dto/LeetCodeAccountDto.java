package com.devhub.leetcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeetCodeAccountDto {
    private boolean connected;
    private String username;
    private Long ranking;
    private int totalSolved;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private int totalActiveDays;
    private int currentStreak;
    private int longestStreak;
    private Map<String, Integer> dailyActivity;
    private Instant connectedAt;
    private Instant lastSyncedAt;
}
