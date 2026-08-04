package com.devhub.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteFolderRepository extends JpaRepository<NoteFolder, UUID> {
    List<NoteFolder> findByUserIdOrderByNameAsc(UUID userId);
}
