package com.devhub.goals;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID> {
    List<Habit> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Habit> findByUserIdAndGoalIdOrderByCreatedAtDesc(UUID userId, UUID goalId);
    List<Habit> findByGoalId(UUID goalId);
}
