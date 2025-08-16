package com.ndash.identity_framework.domain;

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

    @Column
    private String username;

    @Column
    private String password;

    @Column(unique = true)
    private String email;

    // New fields
    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String phoneNumber;

    @Column
    private LocalDateTime dob;

    @Column(unique = true)
    private String ssn; // Social Security Number

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    @Column(name = "azure_id", unique = true)
    private String azureId;

    @Column
    private boolean active = true;
}


