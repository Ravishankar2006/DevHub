package com.devhub.goals;

import com.devhub.common.ApiException;
import com.devhub.goals.dto.GoalDto;
import com.devhub.goals.dto.GoalMapper;
import com.devhub.goals.dto.GoalRequest;
import com.devhub.projects.Project;
import com.devhub.projects.ProjectRepository;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public GoalDto createGoal(User currentUser, GoalRequest request) {
        Project project = resolveProject(currentUser, request.getProjectId());

        Goal goal = Goal.builder()
                .user(currentUser)
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType() : GoalType.MONTHLY)
                .status(request.getStatus() != null ? request.getStatus() : GoalStatus.ACTIVE)
                .targetDate(request.getTargetDate())
                .progressPercent(clampProgress(request.getProgressPercent()))
                .build();

        goal = goalRepository.save(goal);
        return GoalMapper.toDto(goal);
    }

    @Transactional(readOnly = true)
    public List<GoalDto> listGoals(User currentUser, GoalStatus status) {
        List<Goal> goals = status != null
                ? goalRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(currentUser.getId(), status)
                : goalRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId());
        return goals.stream().map(GoalMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GoalDto getGoal(User currentUser, UUID goalId) {
        return GoalMapper.toDto(getOwnedGoal(currentUser, goalId));
    }

    @Transactional
    public GoalDto updateGoal(User currentUser, UUID goalId, GoalRequest request) {
        Goal goal = getOwnedGoal(currentUser, goalId);
        Project project = resolveProject(currentUser, request.getProjectId());

        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setProject(project);
        if (request.getType() != null) {
            goal.setType(request.getType());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }
        goal.setTargetDate(request.getTargetDate());
        goal.setProgressPercent(clampProgress(request.getProgressPercent()));

        goal = goalRepository.save(goal);
        return GoalMapper.toDto(goal);
    }

    @Transactional
    public void deleteGoal(User currentUser, UUID goalId) {
        Goal goal = getOwnedGoal(currentUser, goalId);

        // Unlink habits rather than deleting them - a goal going away shouldn't
        // take its habits with it (SET NULL semantics, applied at the service layer
        // for the same H2-vs-Postgres schema-generation reason as project deletion).
        List<Habit> linkedHabits = habitRepository.findByGoalId(goalId);
        linkedHabits.forEach(habit -> habit.setGoal(null));
        habitRepository.saveAll(linkedHabits);

        goalRepository.delete(goal);
    }

    Goal getOwnedGoal(User currentUser, UUID goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ApiException("Goal not found", HttpStatus.NOT_FOUND));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Goal not found", HttpStatus.NOT_FOUND);
        }
        return goal;
    }

    private Project resolveProject(User currentUser, UUID projectId) {
        if (projectId == null) return null;

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));

        if (!project.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Project not found", HttpStatus.NOT_FOUND);
        }
        return project;
    }

    private int clampProgress(Integer progressPercent) {
        if (progressPercent == null) return 0;
        return Math.max(0, Math.min(100, progressPercent));
    }
}
