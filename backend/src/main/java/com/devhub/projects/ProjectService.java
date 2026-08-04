package com.devhub.projects;

import com.devhub.common.ApiException;
import com.devhub.goals.Goal;
import com.devhub.goals.GoalRepository;
import com.devhub.projects.dto.ProjectDto;
import com.devhub.projects.dto.ProjectMapper;
import com.devhub.projects.dto.ProjectRequest;
import com.devhub.tasks.TaskRepository;
import com.devhub.tasks.TaskStatus;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public ProjectDto createProject(User currentUser, ProjectRequest request) {
        Project project = Project.builder()
                .user(currentUser)
                .name(request.getName())
                .description(request.getDescription())
                .repoUrl(request.getRepoUrl())
                .liveUrl(request.getLiveUrl())
                .roadmap(request.getRoadmap())
                .stackTags(ProjectMapper.joinStackTags(request.getStackTags()))
                .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNING)
                .archived(request.isArchived())
                .build();

        project = projectRepository.save(project);
        return toDto(project);
    }

    public List<ProjectDto> listProjects(User currentUser, boolean includeArchived) {
        List<Project> projects = includeArchived
                ? projectRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId())
                : projectRepository.findByUserIdAndArchivedOrderByUpdatedAtDesc(currentUser.getId(), false);

        return projects.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProjectDto getProject(User currentUser, UUID projectId) {
        return toDto(getOwnedProject(currentUser, projectId));
    }

    @Transactional
    public ProjectDto updateProject(User currentUser, UUID projectId, ProjectRequest request) {
        Project project = getOwnedProject(currentUser, projectId);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setRepoUrl(request.getRepoUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setRoadmap(request.getRoadmap());
        project.setStackTags(ProjectMapper.joinStackTags(request.getStackTags()));
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setArchived(request.isArchived());

        project = projectRepository.save(project);
        return toDto(project);
    }

    @Transactional
    public void deleteProject(User currentUser, UUID projectId) {
        Project project = getOwnedProject(currentUser, projectId);
        // Explicit child cleanup instead of relying on DB-level cascade, since the
        // H2 dev schema (ddl-auto) doesn't carry the ON DELETE CASCADE clauses that
        // the Flyway migration declares for Postgres.
        taskRepository.deleteByProjectId(projectId);
        milestoneRepository.deleteByProjectId(projectId);

        List<Goal> linkedGoals = goalRepository.findByProjectId(projectId);
        linkedGoals.forEach(goal -> goal.setProject(null));
        goalRepository.saveAll(linkedGoals);

        projectRepository.delete(project);
    }

    Project getOwnedProject(User currentUser, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));

        if (!project.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Project not found", HttpStatus.NOT_FOUND);
        }
        return project;
    }

    private ProjectDto toDto(Project project) {
        long taskCount = taskRepository.countByProjectId(project.getId());
        long completedTaskCount = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        long milestoneCount = milestoneRepository.findByProjectIdOrderByDueDateAsc(project.getId()).size();
        return ProjectMapper.toDto(project, taskCount, completedTaskCount, milestoneCount);
    }
}
