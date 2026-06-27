package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;  // e.g. JIRA, Confluence, AWS, etc.

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active = true; // to soft-disable an app if needed

    @Column(name = "app_url", length = 255)
    private String appUrl; // optional - link to application

    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "integration_name")
    private String integrationName;

    @Column(name = "essential")
    private Boolean essential;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}

