package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_application",
                        columnNames = {"user_id", "application_id"}
                )
        }
)
@Getter
@Setter
public class UserApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    /**
     * ID of the user in the external application.
     * Jira -> accountId
     */
    @Column(name = "external_user_id")
    private String externalUserId;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public boolean isCurrentlyAssigned() {
        return active && removedAt == null;
    }
}