package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}