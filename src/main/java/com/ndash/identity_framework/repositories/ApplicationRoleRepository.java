package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.ApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRoleRepository extends JpaRepository<ApplicationRole, Long> {

    Optional<ApplicationRole> findByApplicationIdAndRoleNameIgnoreCase(Long applicationId, String roleName);
}
