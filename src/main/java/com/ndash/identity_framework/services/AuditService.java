package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.AuditEvent;
import com.ndash.identity_framework.repositories.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public void record(final String action, final String entityType, final Long entityId,
                       final Long actorUserId, final String details) {
        final AuditEvent event = new AuditEvent();
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setActorUserId(actorUserId);
        event.setDetails(details);
        event.setOccurredAt(LocalDateTime.now());
        auditEventRepository.save(event);
    }
}
