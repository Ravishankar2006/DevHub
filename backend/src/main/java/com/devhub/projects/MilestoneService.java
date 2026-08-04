package com.devhub.projects;

import com.devhub.common.ApiException;
import com.devhub.projects.dto.MilestoneDto;
import com.devhub.projects.dto.MilestoneMapper;
import com.devhub.projects.dto.MilestoneRequest;
import com.devhub.tasks.Task;
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
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectService projectService;
    private final TaskRepository taskRepository;

    @Transactional
    public MilestoneDto createMilestone(User currentUser, UUID projectId, MilestoneRequest request) {
        Project project = projectService.getOwnedProject(currentUser, projectId);

        Milestone milestone = Milestone.builder()
                .project(project)
                .user(currentUser)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .build();

        milestone = milestoneRepository.save(milestone);
        return toDto(milestone);
    }

    @Transactional(readOnly = true)
    public List<MilestoneDto> listMilestones(User currentUser, UUID projectId) {
        projectService.getOwnedProject(currentUser, projectId);
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MilestoneDto> listAllMilestones(User currentUser) {
        return milestoneRepository.findByUserIdOrderByDueDateAsc(currentUser.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MilestoneDto updateMilestone(User currentUser, UUID projectId, UUID milestoneId, MilestoneRequest request) {
        Milestone milestone = getOwnedMilestone(currentUser, projectId, milestoneId);

        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());

        milestone = milestoneRepository.save(milestone);
        return toDto(milestone);
    }

    @Transactional
    public void deleteMilestone(User currentUser, UUID projectId, UUID milestoneId) {
        Milestone milestone = getOwnedMilestone(currentUser, projectId, milestoneId);

        // Unlink tasks rather than deleting them - a milestone going away shouldn't
        // take its tasks with it (SET NULL semantics, applied at the service layer
        // for the same H2-vs-Postgres schema-generation reason as project deletion).
        List<Task> linkedTasks = taskRepository.findByMilestoneIdOrderByPositionAsc(milestoneId);
        linkedTasks.forEach(task -> task.setMilestone(null));
        taskRepository.saveAll(linkedTasks);

        milestoneRepository.delete(milestone);
    }

    Milestone getOwnedMilestone(User currentUser, UUID projectId, UUID milestoneId) {
        projectService.getOwnedProject(currentUser, projectId);

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ApiException("Milestone not found", HttpStatus.NOT_FOUND));

        if (!milestone.getProject().getId().equals(projectId) || !milestone.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Milestone not found", HttpStatus.NOT_FOUND);
        }
        return milestone;
    }

    private MilestoneDto toDto(Milestone milestone) {
        long totalTasks = taskRepository.countByMilestoneId(milestone.getId());
        long completedTasks = taskRepository.countByMilestoneIdAndStatus(milestone.getId(), TaskStatus.DONE);
        return MilestoneMapper.toDto(milestone, totalTasks, completedTasks);
    }
}
