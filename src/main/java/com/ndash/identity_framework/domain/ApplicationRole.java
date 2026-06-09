package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "application_roles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApplicationRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String roleName;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}
