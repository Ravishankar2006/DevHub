package com.devhub.brief;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyBriefRepository extends JpaRepository<DailyBrief, UUID> {
    Optional<DailyBrief> findByUserIdAndBriefDate(UUID userId, LocalDate briefDate);
}
