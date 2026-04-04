package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "job_titles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "external_source"}))
@Data
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String externalSource;
}