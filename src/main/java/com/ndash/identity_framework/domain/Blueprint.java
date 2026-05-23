package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "blueprints")
@Data
public class Blueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "blueprint_job_titles",
            joinColumns = @JoinColumn(name = "blueprint_id"),
            inverseJoinColumns = @JoinColumn(name = "job_title_id")
    )
    private Set<JobTitle> jobTitles = new HashSet<>();

    @OneToMany(
            mappedBy = "blueprint",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<BlueprintApplicationRole> applicationRoles =
            new HashSet<>();
}
