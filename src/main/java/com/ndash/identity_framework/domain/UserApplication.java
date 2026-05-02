package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "application_id"})
        }
)
@Data
public class UserApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // App
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // Current status
    @Column(nullable = false)
    private boolean active = true;

    // First assigned date
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    // Last removed date
    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    // Utility
    public boolean isCurrentlyAssigned() {
        return active && removedAt == null;
    }
}