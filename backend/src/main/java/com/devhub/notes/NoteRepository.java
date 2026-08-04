package com.devhub.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    List<Note> findByUserIdAndFolderIdOrderByUpdatedAtDesc(UUID userId, UUID folderId);
    List<Note> findByFolderId(UUID folderId);
    long countByFolderId(UUID folderId);

    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND ("
            + "LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(n.tags) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "ORDER BY n.updatedAt DESC")
    List<Note> search(@Param("userId") UUID userId, @Param("q") String q);
}
