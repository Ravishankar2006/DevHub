package com.devhub.projects.dto;

import com.devhub.projects.Project;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static List<String> parseStackTags(String stackTags) {
        if (stackTags == null || stackTags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(stackTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }

    public static String joinStackTags(List<String> stackTags) {
        if (stackTags == null || stackTags.isEmpty()) {
            return null;
        }
        return stackTags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.joining(","));
    }

    public static ProjectDto toDto(Project project, long taskCount, long completedTaskCount, long milestoneCount) {
        if (project == null) return null;

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .repoUrl(project.getRepoUrl())
                .liveUrl(project.getLiveUrl())
                .roadmap(project.getRoadmap())
                .stackTags(parseStackTags(project.getStackTags()))
                .status(project.getStatus().name())
                .archived(project.isArchived())
                .taskCount(taskCount)
                .completedTaskCount(completedTaskCount)
                .milestoneCount(milestoneCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
