package com.devhub.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
    List<DocumentChunk> findByDocumentUserIdAndDocumentStatus(UUID userId, DocumentStatus status);
    void deleteByDocumentId(UUID documentId);
}
