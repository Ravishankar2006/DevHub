package com.devhub.learning.dto;

import com.devhub.learning.LearningResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LearningResourceMapper {

    public static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }

    public static String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.joining(","));
    }

    public static LearningResourceDto toDto(LearningResource resource) {
        if (resource == null) return null;

        return LearningResourceDto.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .resourceType(resource.getResourceType().name())
                .provider(resource.getProvider())
                .url(resource.getUrl())
                .status(resource.getStatus().name())
                .progressPercent(resource.getProgressPercent())
                .estimatedCompletionDate(resource.getEstimatedCompletionDate())
                .tags(parseTags(resource.getTags()))
                .notes(resource.getNotes())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
