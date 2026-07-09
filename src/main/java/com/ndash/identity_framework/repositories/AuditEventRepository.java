package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
