package com.devhub.goals;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "habit_checkins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;
}
