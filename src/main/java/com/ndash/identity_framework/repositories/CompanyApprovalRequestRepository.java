package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.CompanyApprovalRequest;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyApprovalRequestRepository extends JpaRepository<CompanyApprovalRequest, Long> {

    // -------------------------------------------------------
    // Pending approvals for specific approver
    // -------------------------------------------------------
    List<CompanyApprovalRequest> findByApproverIdAndStatusOrderByRequestedAtDesc(
            Long approverId,
            RequestStatus status
    );

    // -------------------------------------------------------
    // Full approval history for approver
    // -------------------------------------------------------
    List<CompanyApprovalRequest> findByApproverIdOrderByRequestedAtDesc(
            Long approverId
    );

    // -------------------------------------------------------
    // Admin - all approvals
    // -------------------------------------------------------
    List<CompanyApprovalRequest> findAllByOrderByRequestedAtDesc();

    // -------------------------------------------------------
    // Optional - company specific approvals
    // -------------------------------------------------------
    List<CompanyApprovalRequest> findByCompanyIdOrderByRequestedAtDesc(
            Long companyId
    );

    // -------------------------------------------------------
    // Optional - requester specific approvals
    // -------------------------------------------------------
    List<CompanyApprovalRequest> findByRequestedByIdOrderByRequestedAtDesc(
            Long requestedById
    );

    // -------------------------------------------------------
    // Optional - Check if company has pending approval
    // Prevent duplicate approval requests
    // -------------------------------------------------------
    boolean existsByCompanyIdAndStatus(
            Long companyId,
            RequestStatus status
    );
}
