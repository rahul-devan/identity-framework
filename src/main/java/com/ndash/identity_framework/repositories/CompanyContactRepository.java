package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {}
