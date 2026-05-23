package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {

    Optional<Blueprint> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("""
        SELECT DISTINCT b
        FROM Blueprint b
        JOIN b.jobTitles jt
        WHERE jt.id = :jobTitleId
    """)
    List<Blueprint> findByJobTitleId(@Param("jobTitleId") Long jobTitleId);

    @Query("""
    SELECT DISTINCT b
    FROM Blueprint b
    LEFT JOIN FETCH b.applicationRoles ar
    LEFT JOIN FETCH ar.application
    JOIN FETCH b.jobTitles jt
    WHERE jt.id = :jobTitleId
""")
    List<Blueprint> findByJobTitleIdWithApps(
            Long jobTitleId
    );
}
