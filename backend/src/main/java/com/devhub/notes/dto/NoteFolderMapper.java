package com.devhub.notes.dto;

import com.devhub.notes.NoteFolder;

public class NoteFolderMapper {

    public static NoteFolderDto toDto(NoteFolder folder, long noteCount) {
        if (folder == null) return null;

        return NoteFolderDto.builder()
                .id(folder.getId())
                .name(folder.getName())
                .noteCount(noteCount)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}
