package com.devhub.tasks;

import com.devhub.common.ApiException;
import com.devhub.projects.Milestone;
import com.devhub.projects.MilestoneRepository;
import com.devhub.projects.Project;
import com.devhub.projects.ProjectRepository;
import com.devhub.tasks.dto.TaskDto;
import com.devhub.tasks.dto.TaskMapper;
import com.devhub.tasks.dto.TaskRequest;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    @Transactional
    public TaskDto createTask(User currentUser, TaskRequest request) {
        Milestone milestone = resolveMilestone(currentUser, request.getMilestoneId());
        Project project = milestone != null ? milestone.getProject() : resolveProject(currentUser, request.getProjectId());

        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.TODO;
        int position = (int) taskRepository.countByUserIdAndStatus(currentUser.getId(), status);

        Task task = Task.builder()
                .user(currentUser)
                .project(project)
                .milestone(milestone)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .position(position)
                .build();

        task = taskRepository.save(task);
        return TaskMapper.toDto(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> listTasks(User currentUser, UUID projectId, TaskStatus status) {
        List<Task> tasks;
        if (projectId != null && status != null) {
            tasks = taskRepository.findByUserIdAndProjectIdAndStatusOrderByPositionAsc(currentUser.getId(), projectId, status);
        } else if (projectId != null) {
            tasks = taskRepository.findByUserIdAndProjectIdOrderByPositionAsc(currentUser.getId(), projectId);
        } else if (status != null) {
            tasks = taskRepository.findByUserIdAndStatusOrderByPositionAsc(currentUser.getId(), status);
        } else {
            tasks = taskRepository.findByUserIdOrderByPositionAsc(currentUser.getId());
        }
        return tasks.stream().map(TaskMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(User currentUser, UUID taskId) {
        return TaskMapper.toDto(getOwnedTask(currentUser, taskId));
    }

    @Transactional
    public TaskDto updateTask(User currentUser, UUID taskId, TaskRequest request) {
        Task task = getOwnedTask(currentUser, taskId);

        Milestone milestone = resolveMilestone(currentUser, request.getMilestoneId());
        Project project = milestone != null ? milestone.getProject() : resolveProject(currentUser, request.getProjectId());

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setMilestone(milestone);
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());

        task = taskRepository.save(task);
        return TaskMapper.toDto(task);
    }

    @Transactional
    public void deleteTask(User currentUser, UUID taskId) {
        Task task = getOwnedTask(currentUser, taskId);
        taskRepository.delete(task);
    }

    private Task getOwnedTask(User currentUser, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Task not found", HttpStatus.NOT_FOUND);
        }
        return task;
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

    private Milestone resolveMilestone(User currentUser, UUID milestoneId) {
        if (milestoneId == null) return null;

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ApiException("Milestone not found", HttpStatus.NOT_FOUND));

        if (!milestone.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Milestone not found", HttpStatus.NOT_FOUND);
        }
        return milestone;
    }
}
