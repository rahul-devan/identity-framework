package com.ndash.identity_framework.domain;

import com.ndash.identity_framework.domain.enums.UserSource;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -----------------------------
    // Basic Info
    // -----------------------------
    @Column
    private String username;

    @Column
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String phoneNumber;

    @Column
    private LocalDateTime dob;

    @Column(unique = true)
    @Convert(converter = com.ndash.identity_framework.util.SsnEncryptionConverter.class)
    private String ssn;

    // -----------------------------
    // Identity Providers
    // -----------------------------
    @Column(name = "azure_id", unique = true)
    private String azureId;

    @Column(name = "external_id")
    private String externalId; // HR system ID

    @Column(name = "external_source")
    private String externalSource; // ODOO / WORKDAY / etc

    // -----------------------------
    // HR Info
    // -----------------------------
    @Column
    private String jobTitleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // -----------------------------
    // System Info
    // -----------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserSource source; // APP / ENTRA / HR

    @Column
    private boolean active = true;

    @Column
    private LocalDateTime lastSyncedAt;

    // -----------------------------
    // Roles
    // -----------------------------
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();


    @ManyToOne
    @JoinColumn(name = "blueprint_id")
    private Blueprint blueprint;

    @ManyToOne
    @JoinColumn(name = "job_title_id")
    private JobTitle jobTitle;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "manager")
    private Set<User> subordinates = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "country_code")
    private String countryCode;


}


