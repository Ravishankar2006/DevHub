package com.devhub.learning;

import com.devhub.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "learning_resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LearningResourceType resourceType = LearningResourceType.COURSE;

    private String provider;

    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LearningStatus status = LearningStatus.NOT_STARTED;

    @Column(name = "progress_percent", nullable = false)
    @Builder.Default
    private int progressPercent = 0;

    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
