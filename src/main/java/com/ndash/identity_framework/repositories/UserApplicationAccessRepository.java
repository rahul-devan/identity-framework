package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.UserApplicationAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserApplicationAccessRepository
        extends JpaRepository<UserApplicationAccess, Long> {

    List<UserApplicationAccess> findByUserApplicationIdAndActiveTrue(
            Long userApplicationId
    );

    Optional<UserApplicationAccess> findByUserApplicationIdAndExternalProjectIdAndExternalRoleId(
            Long userApplicationId,
            String projectId,
            String roleId
    );
}
