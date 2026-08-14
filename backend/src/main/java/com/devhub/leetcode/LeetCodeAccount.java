package com.devhub.leetcode;

import com.devhub.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leetcode_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LeetCodeAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "leetcode_username", nullable = false)
    private String leetcodeUsername;

    @Column(name = "ranking")
    private Long ranking;

    @Column(name = "total_solved", nullable = false)
    @Builder.Default
    private int totalSolved = 0;

    @Column(name = "easy_solved", nullable = false)
    @Builder.Default
    private int easySolved = 0;

    @Column(name = "medium_solved", nullable = false)
    @Builder.Default
    private int mediumSolved = 0;

    @Column(name = "hard_solved", nullable = false)
    @Builder.Default
    private int hardSolved = 0;

    @Column(name = "total_active_days", nullable = false)
    @Builder.Default
    private int totalActiveDays = 0;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private int longestStreak = 0;

    @Column(name = "daily_activity_json", columnDefinition = "TEXT")
    private String dailyActivityJson;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
