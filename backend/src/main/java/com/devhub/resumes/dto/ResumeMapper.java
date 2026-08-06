package com.devhub.resumes.dto;

import com.devhub.resumes.Resume;

public class ResumeMapper {

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
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
