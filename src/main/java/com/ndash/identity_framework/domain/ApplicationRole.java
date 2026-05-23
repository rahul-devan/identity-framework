package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "application_roles")
@Data
public class ApplicationRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}
