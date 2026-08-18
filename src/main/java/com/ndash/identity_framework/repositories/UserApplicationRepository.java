package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.UserApplication;
import com.ndash.identity_framework.dto.UserApplicationDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository
        extends JpaRepository<UserApplication, Long> {

    @Query("""
            SELECT new com.ndash.identity_framework.dto.UserApplicationDto(
                ua.id, a.id, a.name, a.description, ua.assignedAt, ua.active
            )
            FROM UserApplication ua
            JOIN ua.application a
            WHERE ua.user.id = :userId AND ua.active = true
            """)
    List<UserApplicationDto> findActiveApplicationDtosByUserId(@Param("userId") Long userId);

    // Active apps for user
    @EntityGraph(attributePaths = {"application"})
    List<UserApplication> findByUserIdAndActiveTrue(Long userId);

    // All apps for user
    List<UserApplication> findByUserId(Long userId);

    // Specific user + app
    Optional<UserApplication> findByUserIdAndApplicationId(
            Long userId,
            Long applicationId
    );
}