package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Role> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);
}

