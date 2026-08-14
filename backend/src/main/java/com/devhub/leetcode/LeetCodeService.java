package com.devhub.leetcode;

import com.devhub.common.ApiException;
import com.devhub.common.StreakCalculator;
import com.devhub.jobs.AiJob;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.jobs.dto.AiJobMapper;
import com.devhub.leetcode.dto.LeetCodeAccountDto;
import com.devhub.leetcode.dto.LeetCodeMapper;
import com.devhub.users.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeetCodeService {

    private static final int ACTIVITY_HISTORY_DAYS = 60;

    private final LeetCodeAccountRepository leetCodeAccountRepository;
    private final LeetCodeApiClient leetCodeApiClient;
    private final AiJobService aiJobService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LeetCodeAccountDto connect(User currentUser, String username) {
        String trimmed = username == null ? "" : username.trim();
        if (trimmed.isEmpty()) {
            throw new ApiException("LeetCode username is required.", HttpStatus.BAD_REQUEST);
        }

        LeetCodeApiClient.MatchedUser profile = leetCodeApiClient.fetchProfile(trimmed);
        if (profile == null) {
            throw new ApiException("No LeetCode user found with that username.", HttpStatus.BAD_REQUEST);
        }

        LeetCodeAccount account = leetCodeAccountRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> LeetCodeAccount.builder().user(currentUser).build());
        account.setLeetcodeUsername(profile.username());
        if (account.getConnectedAt() == null) {
            account.setConnectedAt(Instant.now());
        }
        applyProfile(account, profile);
        account.setLastSyncedAt(Instant.now());
        leetCodeAccountRepository.save(account);

        return LeetCodeMapper.toDto(account);
    }

    @Transactional
    public AiJobDto triggerManualSync(User currentUser) {
        leetCodeAccountRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ApiException("Connect your LeetCode account first.", HttpStatus.BAD_REQUEST));

        AiJob job = aiJobService.createJob(currentUser, AiJobType.LEETCODE_SYNC, currentUser.getId());
        return AiJobMapper.toDto(job);
    }

    @Transactional
    public void performSync(AiJob job) {
        UUID userId = job.getTargetId();
        LeetCodeAccount account = leetCodeAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("LeetCode account is no longer connected.", HttpStatus.NOT_FOUND));

        LeetCodeApiClient.MatchedUser profile = leetCodeApiClient.fetchProfile(account.getLeetcodeUsername());
        if (profile == null) {
            throw new ApiException(
                    "LeetCode user " + account.getLeetcodeUsername() + " could not be found.", HttpStatus.BAD_GATEWAY);
        }

        applyProfile(account, profile);
        account.setLastSyncedAt(Instant.now());
        leetCodeAccountRepository.save(account);
        log.info("LeetCode sync completed for account {}", account.getId());
    }

    @Transactional(readOnly = true)
    public LeetCodeAccountDto getAccountStatus(User currentUser) {
        return leetCodeAccountRepository.findByUserId(currentUser.getId())
                .map(LeetCodeMapper::toDto)
                .orElseGet(LeetCodeMapper::disconnected);
    }

    @Transactional
    public void disconnect(User currentUser) {
        LeetCodeAccount account = leetCodeAccountRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ApiException("LeetCode account is not connected.", HttpStatus.NOT_FOUND));
        leetCodeAccountRepository.delete(account);
    }

    private void applyProfile(LeetCodeAccount account, LeetCodeApiClient.MatchedUser profile) {
        account.setRanking(profile.profile() != null ? profile.profile().ranking() : null);

        Map<String, Integer> byDifficulty = new HashMap<>();
        if (profile.submitStats() != null && profile.submitStats().acSubmissionNum() != null) {
            for (LeetCodeApiClient.AcSubmission sub : profile.submitStats().acSubmissionNum()) {
                if (sub.difficulty() != null && sub.count() != null) {
                    byDifficulty.put(sub.difficulty(), sub.count());
                }
            }
        }
        account.setEasySolved(byDifficulty.getOrDefault("Easy", 0));
        account.setMediumSolved(byDifficulty.getOrDefault("Medium", 0));
        account.setHardSolved(byDifficulty.getOrDefault("Hard", 0));
        account.setTotalSolved(byDifficulty.getOrDefault("All",
                account.getEasySolved() + account.getMediumSolved() + account.getHardSolved()));

        Map<LocalDate, Integer> dailySubmissions = parseSubmissionCalendar(
                profile.userCalendar() != null ? profile.userCalendar().submissionCalendar() : null);
        Set<Long> buckets = dailySubmissions.keySet().stream().map(LocalDate::toEpochDay).collect(Collectors.toSet());

        account.setTotalActiveDays(buckets.size());
        account.setCurrentStreak((int) StreakCalculator.computeCurrentStreak(buckets));
        account.setLongestStreak((int) StreakCalculator.computeLongestStreak(buckets));

        LocalDate historyCutoff = LocalDate.now().minusDays(ACTIVITY_HISTORY_DAYS - 1);
        Map<String, Integer> recentActivity = dailySubmissions.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(historyCutoff))
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue, (a, b) -> a, TreeMap::new));
        account.setDailyActivityJson(writeJson(recentActivity));
    }

    /** LeetCode encodes the calendar as a JSON string keyed by UTC-midnight unix seconds. */
    private Map<LocalDate, Integer> parseSubmissionCalendar(String submissionCalendarJson) {
        if (submissionCalendarJson == null || submissionCalendarJson.isBlank()) return Map.of();
        try {
            Map<String, Integer> raw =
                    objectMapper.readValue(submissionCalendarJson, new TypeReference<Map<String, Integer>>() {});
            Map<LocalDate, Integer> result = new TreeMap<>();
            raw.forEach((epochSeconds, count) -> {
                LocalDate day = Instant.ofEpochSecond(Long.parseLong(epochSeconds)).atZone(ZoneOffset.UTC).toLocalDate();
                result.merge(day, count, Integer::sum);
            });
            return result;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
