package com.devhub.notes.dto;

import com.devhub.notes.Note;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NoteMapper {

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

    public static NoteDto toDto(Note note) {
        if (note == null) return null;

        return NoteDto.builder()
                .id(note.getId())
                .folderId(note.getFolder() != null ? note.getFolder().getId() : null)
                .folderName(note.getFolder() != null ? note.getFolder().getName() : null)
                .title(note.getTitle())
                .content(note.getContent())
                .tags(parseTags(note.getTags()))
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
