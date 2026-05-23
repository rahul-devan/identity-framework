package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.ApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRoleRepository
        extends JpaRepository<ApplicationRole, Long> {
}
