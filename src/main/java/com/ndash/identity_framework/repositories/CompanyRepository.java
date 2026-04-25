package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {}

