package com.devhub.leetcode.dto;

import com.devhub.leetcode.LeetCodeAccount;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class LeetCodeMapper {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int DASHBOARD_ACTIVITY_DAYS = 30;

    public static LeetCodeAccountDto toDto(LeetCodeAccount account) {
        return LeetCodeAccountDto.builder()
                .connected(true)
                .username(account.getLeetcodeUsername())
                .ranking(account.getRanking())
                .totalSolved(account.getTotalSolved())
                .easySolved(account.getEasySolved())
                .mediumSolved(account.getMediumSolved())
                .hardSolved(account.getHardSolved())
                .totalActiveDays(account.getTotalActiveDays())
                .currentStreak(account.getCurrentStreak())
                .longestStreak(account.getLongestStreak())
                .dailyActivity(recentActivity(account.getDailyActivityJson()))
                .connectedAt(account.getConnectedAt())
                .lastSyncedAt(account.getLastSyncedAt())
                .build();
    }

    public static LeetCodeAccountDto disconnected() {
        return LeetCodeAccountDto.builder()
                .connected(false)
                .dailyActivity(Map.of())
                .build();
    }

    private static Map<String, Integer> recentActivity(String dailyActivityJson) {
        Map<String, Integer> parsed;
        try {
            parsed = dailyActivityJson == null ? Map.of()
                    : JSON.readValue(dailyActivityJson, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            parsed = Map.of();
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(DASHBOARD_ACTIVITY_DAYS - 1);
        for (int i = 0; i < DASHBOARD_ACTIVITY_DAYS; i++) {
            String key = start.plusDays(i).toString();
            result.put(key, parsed.getOrDefault(key, 0));
        }
        return result;
    }
}
