package com.devhub.resumes.dto;

import com.devhub.resumes.Resume;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ResumeMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ResumeDto toDto(Resume resume) {
        if (resume == null) return null;

        return ResumeDto.builder()
                .id(resume.getId())
                .name(resume.getName())
                .label(resume.getLabel())
                .fileName(resume.getFileName())
                .fileSizeBytes(resume.getFileSizeBytes())
                .notes(resume.getNotes())
                .downloadUrl("/resumes/" + resume.getId() + "/download")
                .reviewScore(resume.getReviewScore())
                .reviewSummary(resume.getReviewSummary())
                .reviewIssues(parseStringList(resume.getReviewIssues()))
                .reviewSuggestions(parseStringList(resume.getReviewSuggestions()))
                .reviewedAt(resume.getReviewedAt())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    private static List<String> parseStringList(String json) {
        if (json == null) return List.of();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
