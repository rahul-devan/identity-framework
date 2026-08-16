package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.UserApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository
        extends JpaRepository<UserApplication, Long> {

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