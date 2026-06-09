package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blueprint_application_roles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BlueprintApplicationRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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