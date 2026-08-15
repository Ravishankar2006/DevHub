package com.devhub.brief;

import com.devhub.ai.GeminiChatClient;
import com.devhub.brief.dto.DailyBriefDto;
import com.devhub.brief.dto.DailyBriefMapper;
import com.devhub.calendar.CalendarEvent;
import com.devhub.calendar.CalendarEventRepository;
import com.devhub.careers.JobApplication;
import com.devhub.careers.JobApplicationRepository;
import com.devhub.common.ApiException;
import com.devhub.github.GitHubAccountRepository;
import com.devhub.github.GitHubRepo;
import com.devhub.github.GitHubRepoRepository;
import com.devhub.goals.Goal;
import com.devhub.goals.GoalRepository;
import com.devhub.goals.GoalStatus;
import com.devhub.jobs.AiJob;
import com.devhub.jobs.AiJobService;
import com.devhub.jobs.AiJobType;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.jobs.dto.AiJobMapper;
import com.devhub.learning.LearningResource;
import com.devhub.learning.LearningResourceRepository;
import com.devhub.learning.LearningStatus;
import com.devhub.leetcode.LeetCodeAccountRepository;
import com.devhub.tasks.Task;
import com.devhub.tasks.TaskRepository;
import com.devhub.tasks.TaskStatus;
import com.devhub.users.User;
import com.devhub.users.UserRepository;
import com.devhub.users.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBriefService {

    private static final int LOOKAHEAD_DAYS = 7;

    private final DailyBriefRepository dailyBriefRepository;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final AiJobService aiJobService;
    private final GeminiChatClient geminiChatClient;
    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final LearningResourceRepository learningResourceRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final GitHubAccountRepository gitHubAccountRepository;
    private final GitHubRepoRepository gitHubRepoRepository;
    private final LeetCodeAccountRepository leetCodeAccountRepository;

    @Transactional
    public AiJobDto triggerGeneration(User currentUser) {
        boolean enabled = userSettingsRepository.findByUserId(currentUser.getId())
                .map(settings -> settings.isDailyBriefEnabled())
                .orElse(true);
        if (!enabled) {
            throw new ApiException("Daily brief is disabled in settings", HttpStatus.BAD_REQUEST);
        }

        AiJob job = aiJobService.createJob(currentUser, AiJobType.DAILY_BRIEF, currentUser.getId());
        return AiJobMapper.toDto(job);
    }

    @Transactional(readOnly = true)
    public DailyBriefDto getTodayBrief(User currentUser) {
        DailyBrief brief = dailyBriefRepository.findByUserIdAndBriefDate(currentUser.getId(), LocalDate.now())
                .orElseThrow(() -> new ApiException("No daily brief generated yet today", HttpStatus.NOT_FOUND));
        return DailyBriefMapper.toDto(brief);
    }

    @Transactional
    public void performGeneration(AiJob job) {
        UUID userId = job.getTargetId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User no longer exists", HttpStatus.NOT_FOUND));

        String summary = buildDataSummary(userId);
        String content = geminiChatClient.generateDailyBrief(summary);

        LocalDate today = LocalDate.now();
        DailyBrief brief = dailyBriefRepository.findByUserIdAndBriefDate(userId, today)
                .orElseGet(() -> DailyBrief.builder().user(user).briefDate(today).build());
        brief.setContent(content);
        brief.setGeneratedAt(Instant.now());
        dailyBriefRepository.save(brief);
        log.info("Daily brief generated for user {}", userId);
    }

    private String buildDataSummary(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(LOOKAHEAD_DAYS);
        Instant now = Instant.now();
        Instant weekOut = now.plus(LOOKAHEAD_DAYS, ChronoUnit.DAYS);

        StringBuilder sb = new StringBuilder();

        List<Task> tasks = taskRepository.findByUserIdOrderByPositionAsc(userId).stream()
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isAfter(horizon) && t.getStatus() != TaskStatus.DONE)
                .toList();
        appendSection(sb, "Tasks due within 7 days", tasks.stream()
                .map(t -> t.getTitle() + " (due " + t.getDueDate() + ", " + t.getStatus() + ")")
                .toList());

        List<Goal> goals = goalRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, GoalStatus.ACTIVE);
        appendSection(sb, "Active goals", goals.stream()
                .map(g -> g.getTitle() + " (" + g.getProgressPercent() + "% complete"
                        + (g.getTargetDate() != null ? ", target " + g.getTargetDate() : "") + ")")
                .toList());

        List<JobApplication> deadlines = jobApplicationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .filter(j -> j.getDeadline() != null && !j.getDeadline().isBefore(today) && !j.getDeadline().isAfter(horizon))
                .toList();
        appendSection(sb, "Job application deadlines within 7 days", deadlines.stream()
                .map(j -> j.getRoleTitle() + " at " + j.getCompany().getName() + " (deadline " + j.getDeadline() + ")")
                .toList());

        List<LearningResource> learning = learningResourceRepository
                .findByUserIdAndStatusOrderByUpdatedAtDesc(userId, LearningStatus.IN_PROGRESS);
        appendSection(sb, "Learning items in progress", learning.stream()
                .map(LearningResource::getTitle)
                .toList());

        List<CalendarEvent> events = calendarEventRepository
                .findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, now, weekOut);
        appendSection(sb, "Calendar events within 7 days", events.stream()
                .map(e -> e.getTitle() + " (" + e.getStartTime() + ")")
                .toList());

        gitHubAccountRepository.findByUserId(userId).ifPresent(account -> {
            List<GitHubRepo> repos = gitHubRepoRepository.findByGithubAccountIdOrderByPushedAtDesc(account.getId());
            int totalStars = repos.stream().mapToInt(GitHubRepo::getStars).sum();
            long originalCount = repos.stream().filter(r -> !r.isFork()).count();
            long forkedCount = repos.size() - originalCount;
            appendSection(sb, "GitHub activity", List.of(
                    "Current streak: " + account.getCurrentStreak() + " day(s), longest streak: " + account.getLongestStreak() + " day(s)",
                    "Commits in last 30 days: " + account.getCommitsLast30Days(),
                    repos.size() + " repositories (" + originalCount + " original, " + forkedCount + " forked), " + totalStars + " total stars"));
        });

        leetCodeAccountRepository.findByUserId(userId).ifPresent(account -> {
            List<String> lines = new ArrayList<>(List.of(
                    "Current streak: " + account.getCurrentStreak() + " day(s), longest streak: " + account.getLongestStreak() + " day(s)",
                    "Total solved: " + account.getTotalSolved() + " (" + account.getEasySolved() + " easy, "
                            + account.getMediumSolved() + " medium, " + account.getHardSolved() + " hard)"));
            if (account.getRanking() != null) {
                lines.add("Ranking: " + account.getRanking());
            }
            appendSection(sb, "LeetCode activity", lines);
        });

        if (sb.isEmpty()) {
            return "The user has no tasks, goals, deadlines, learning items, calendar events, or connected coding activity in the next 7 days.";
        }
        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String heading, List<String> items) {
        if (items.isEmpty()) return;
        sb.append(heading).append(":\n");
        items.forEach(item -> sb.append("- ").append(item).append('\n'));
        sb.append('\n');
    }
}
