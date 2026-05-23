package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "blueprint_application_roles")
@Data
public class BlueprintApplicationRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blueprint_id")
    private Blueprint blueprint;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;


    @Column(nullable = false)
    private String roleName;
}