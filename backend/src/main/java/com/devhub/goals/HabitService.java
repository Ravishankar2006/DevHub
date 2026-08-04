package com.devhub.goals;

import com.devhub.common.ApiException;
import com.devhub.goals.dto.HabitDto;
import com.devhub.goals.dto.HabitMapper;
import com.devhub.goals.dto.HabitRequest;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCheckinRepository habitCheckinRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public HabitDto createHabit(User currentUser, HabitRequest request) {
        Goal goal = resolveGoal(currentUser, request.getGoalId());

        Habit habit = Habit.builder()
                .user(currentUser)
                .goal(goal)
                .title(request.getTitle())
                .frequency(request.getFrequency() != null ? request.getFrequency() : HabitFrequency.DAILY)
                .build();

        habit = habitRepository.save(habit);
        return toDto(habit);
    }

    @Transactional(readOnly = true)
    public List<HabitDto> listHabits(User currentUser, UUID goalId) {
        List<Habit> habits = goalId != null
                ? habitRepository.findByUserIdAndGoalIdOrderByCreatedAtDesc(currentUser.getId(), goalId)
                : habitRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return habits.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HabitDto getHabit(User currentUser, UUID habitId) {
        return toDto(getOwnedHabit(currentUser, habitId));
    }

    @Transactional
    public HabitDto updateHabit(User currentUser, UUID habitId, HabitRequest request) {
        Habit habit = getOwnedHabit(currentUser, habitId);
        Goal goal = resolveGoal(currentUser, request.getGoalId());

        habit.setTitle(request.getTitle());
        habit.setGoal(goal);
        if (request.getFrequency() != null) {
            habit.setFrequency(request.getFrequency());
        }

        habit = habitRepository.save(habit);
        return toDto(habit);
    }

    @Transactional
    public void deleteHabit(User currentUser, UUID habitId) {
        Habit habit = getOwnedHabit(currentUser, habitId);
        habitCheckinRepository.deleteByHabitId(habitId);
        habitRepository.delete(habit);
    }

    @Transactional
    public HabitDto toggleTodayCheckin(User currentUser, UUID habitId) {
        Habit habit = getOwnedHabit(currentUser, habitId);
        LocalDate today = LocalDate.now();

        habitCheckinRepository.findByHabitIdAndCheckinDate(habitId, today)
                .ifPresentOrElse(
                        habitCheckinRepository::delete,
                        () -> habitCheckinRepository.save(HabitCheckin.builder().habit(habit).checkinDate(today).build())
                );

        return toDto(habit);
    }

    Habit getOwnedHabit(User currentUser, UUID habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ApiException("Habit not found", HttpStatus.NOT_FOUND));

        if (!habit.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Habit not found", HttpStatus.NOT_FOUND);
        }
        return habit;
    }

    private Goal resolveGoal(User currentUser, UUID goalId) {
        if (goalId == null) return null;

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ApiException("Goal not found", HttpStatus.NOT_FOUND));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Goal not found", HttpStatus.NOT_FOUND);
        }
        return goal;
    }

    private HabitDto toDto(Habit habit) {
        List<LocalDate> checkinDates = habitCheckinRepository.findByHabitIdOrderByCheckinDateDesc(habit.getId())
                .stream().map(HabitCheckin::getCheckinDate).collect(Collectors.toList());

        Set<Long> buckets = checkinDates.stream()
                .map(date -> toBucket(date, habit.getFrequency()))
                .collect(Collectors.toCollection(HashSet::new));

        boolean checkedInToday = checkinDates.contains(LocalDate.now());
        int currentStreak = (int) computeCurrentStreak(buckets, habit.getFrequency());
        int longestStreak = (int) computeLongestStreak(buckets);

        return HabitMapper.toDto(habit, currentStreak, longestStreak, checkedInToday);
    }

    private long toBucket(LocalDate date, HabitFrequency frequency) {
        return frequency == HabitFrequency.WEEKLY ? Math.floorDiv(date.toEpochDay(), 7) : date.toEpochDay();
    }

    private long computeCurrentStreak(Set<Long> buckets, HabitFrequency frequency) {
        long todayBucket = toBucket(LocalDate.now(), frequency);
        long cursor;
        if (buckets.contains(todayBucket)) {
            cursor = todayBucket;
        } else if (buckets.contains(todayBucket - 1)) {
            cursor = todayBucket - 1;
        } else {
            return 0;
        }

        long streak = 0;
        while (buckets.contains(cursor)) {
            streak++;
            cursor--;
        }
        return streak;
    }

    private long computeLongestStreak(Set<Long> buckets) {
        long longest = 0;
        for (Long bucket : buckets) {
            if (buckets.contains(bucket - 1)) continue; // not the start of a run
            long run = 1;
            long cursor = bucket + 1;
            while (buckets.contains(cursor)) {
                run++;
                cursor++;
            }
            longest = Math.max(longest, run);
        }
        return longest;
    }
}
