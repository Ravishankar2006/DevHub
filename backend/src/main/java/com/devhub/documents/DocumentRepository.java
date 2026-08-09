package com.devhub.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByUserIdAndSourceTypeOrderByUpdatedAtDesc(UUID userId, DocumentSourceType sourceType);
    Optional<Document> findBySourceTypeAndSourceId(DocumentSourceType sourceType, UUID sourceId);
}
