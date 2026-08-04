package com.devhub.goals;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitCheckinRepository extends JpaRepository<HabitCheckin, UUID> {
    List<HabitCheckin> findByHabitIdOrderByCheckinDateDesc(UUID habitId);
    Optional<HabitCheckin> findByHabitIdAndCheckinDate(UUID habitId, LocalDate checkinDate);
    void deleteByHabitId(UUID habitId);
}
