package com.ndash.identity_framework.domain;

import com.ndash.identity_framework.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "delegate_requests")
@Data
public class DelegateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who is requesting
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // Target department
    @ManyToOne
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    // Status
    @Enumerated(EnumType.STRING)
    private RequestStatus status; // PENDING, APPROVED, REJECTED

    // Who approved/rejected
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actioned_by")
    private User actionedBy;

    private String comments;

    private LocalDateTime requestedAt;
    private LocalDateTime actionedAt;
}
