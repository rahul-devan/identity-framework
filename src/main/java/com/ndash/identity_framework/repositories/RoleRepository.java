package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

