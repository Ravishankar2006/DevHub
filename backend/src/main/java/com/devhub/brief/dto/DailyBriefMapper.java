package com.devhub.brief.dto;

import com.devhub.brief.DailyBrief;

public class DailyBriefMapper {

    public static DailyBriefDto toDto(DailyBrief brief) {
        if (brief == null) return null;

        return DailyBriefDto.builder()
                .id(brief.getId())
                .briefDate(brief.getBriefDate())
                .content(brief.getContent())
                .generatedAt(brief.getGeneratedAt())
                .build();
    }
}
