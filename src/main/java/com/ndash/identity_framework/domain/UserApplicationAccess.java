package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_application_access",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_app_project_role",
                        columnNames = {
                                "user_application_id",
                                "external_project_id",
                                "external_role_id"
                        }
                )
        }
)
@Getter
@Setter
public class UserApplicationAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_application_id", nullable = false)
    private UserApplication userApplication;

    /**
     * Jira project ID
     */
    @Column(name = "external_project_id", nullable = false)
    private String externalProjectId;

    /**
     * Jira project key, e.g. SCRUM
     */
    @Column(name = "external_project_key")
    private String externalProjectKey;

    /**
     * Jira role ID, e.g. 10002
     */
    @Column(name = "external_role_id", nullable = false)
    private String externalRoleId;

    /**
     * Jira role name, e.g. Member
     */
    @Column(name = "external_role_name")
    private String externalRoleName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
