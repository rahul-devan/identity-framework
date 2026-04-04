package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // External mapping (VERY IMPORTANT for generic HR support)
    @Column(name = "external_id")
    private String externalId;

    @Column(name = "external_source")
    private String externalSource;

    // Optional: reverse mapping (not mandatory)
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
}
