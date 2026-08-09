package com.devhub.documents.dto;

import com.devhub.documents.Document;

public class DocumentMapper {

    public static DocumentDto toDto(Document document) {
        if (document == null) return null;

        return DocumentDto.builder()
                .id(document.getId())
                .title(document.getTitle())
                .fileName(document.getFileName())
                .status(document.getStatus().name())
                .indexedAt(document.getIndexedAt())
                .errorMessage(document.getErrorMessage())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
