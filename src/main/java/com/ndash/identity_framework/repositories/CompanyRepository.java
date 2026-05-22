package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByApproverId(Long approverId);
}

